package io.ktor.utils.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.internal.*
import io.ktor.utils.io.pool.*
import kotlinx.atomicfu.*
import kotlinx.atomicfu.locks.*
import kotlinx.coroutines.*
import kotlin.math.*

private const val EXPECTED_CAPACITY: Long = 4088L

/**
 * Sequential (non-concurrent) byte channel implementation
 */
@Suppress("OverridingDeprecatedMember")
public class ByteChannelSequentialBase(
    initial: ChunkBuffer,
    override val autoFlush: Boolean,
    pool: ObjectPool<ChunkBuffer> = ChunkBuffer.Pool
) : ByteChannel, ByteReadChannel, ByteWriteChannel {
    private val _lastReadView: AtomicRef<ChunkBuffer> = atomic(ChunkBuffer.Empty)

    private val _totalBytesRead = atomic(0L)
    private val _totalBytesWritten = atomic(0L)
    private val _availableForRead = atomic(0)
    private val channelSize = atomic(0)

    private val _closed = atomic<CloseElement?>(null)
    private val isCancelled: Boolean get() = _closed.value?.cause != null

    protected var closed: Boolean
        get() = _closed.value != null
        set(_) {
            error("Setting is not allowed for closed")
        }

    protected val writable: BytePacketBuilder = BytePacketBuilder(pool)
    protected val readable: ByteReadPacket = ByteReadPacket(initial, pool)

    private var lastReadAvailable: Int by atomic(0)
    private var lastReadView: ChunkBuffer by atomic(ChunkBuffer.Empty)

    private val slot = AwaitingSlot()

    override fun attachJob(job: Job) {
        TODO("Not yet implemented")
    }

    override val availableForRead: Int get() = _availableForRead.value

    override val availableForWrite: Int
        get() = maxOf(0, EXPECTED_CAPACITY.toInt() - channelSize.value)

    override val isClosedForRead: Boolean
        get() = isCancelled || (closed && channelSize.value == 0)

    override val isClosedForWrite: Boolean
        get() = closed

    override val totalBytesRead: Long
        get() = _totalBytesRead.value

    override suspend fun peek(): ChunkBuffer {
        TODO("Not yet implemented")
    }

    override suspend fun read(buffer: ChunkBuffer) {
        TODO("Not yet implemented")
    }

    override val totalBytesWritten: Long get() = _totalBytesWritten.value

    final override var closedCause: Throwable?
        get() = _closed.value?.cause
        set(_) {
            error("Closed cause shouldn't be changed directly")
        }

    private val flushMutex = SynchronizedObject()
    private val flushBuffer: BytePacketBuilder = BytePacketBuilder()

    init {
        val count = initial.remainingAll().toInt()
        afterWrite(count)
        _availableForRead.addAndGet(count)
    }

    internal suspend fun awaitAtLeastNBytesAvailableForWrite(count: Int) {
        while (availableForWrite < count && !closed) {
            if (!flushImpl()) {
                slot.sleep { availableForWrite < count && !closed }
            }
        }
    }

    internal suspend fun awaitAtLeastNBytesAvailableForRead(count: Int) {
        while (availableForRead < count && !isClosedForRead) {
            slot.sleep { availableForRead < count && !isClosedForRead }
        }
    }

    override suspend fun flush() {
        flushImpl()
    }

    private fun flushImpl(): Boolean {
        if (writable.isEmpty) {
            slot.resume()
            return false
        }

        flushWrittenBytes()
        slot.resume()
        return true
    }

    /**
     * Send bytes to thread-safe storage.
     *
     * This method is writer-only safe.
     */
    private fun flushWrittenBytes() {
        synchronized(flushMutex) {
            val size = writable.size
            val buffer = writable.stealAll()!!
            flushBuffer.writeChunkBuffer(buffer)
            _availableForRead.addAndGet(size)
        }
    }

    /**
     * Take flushed bytes before read.
     *
     * This method is reader-only safe.
     */
    protected fun prepareFlushedBytes() {
        synchronized(flushMutex) {
            readable.unsafeAppend(flushBuffer)
        }
    }

    private fun ensureNotClosed() {
        if (closed) {
            throw closedCause ?: ClosedWriteChannelException("Channel $this is already closed")
        }
    }

    protected fun afterRead(count: Int) {
        addBytesRead(count)
        slot.resume()
    }

    protected fun readAvailableClosed(): Int {
        closedCause?.let { throw it }

        if (availableForRead > 0) {
            prepareFlushedBytes()
        }

        return -1
    }

    internal suspend fun readAvailable(dst: Buffer): Int {
        closedCause?.let { throw it }
        if (closed && availableForRead == 0) return -1

        if (dst.writeRemaining == 0) return 0

        if (availableForRead == 0) {
            awaitSuspend(1)
        }

        if (!readable.canRead()) {
            prepareFlushedBytes()
        }

        val size = minOf(dst.writeRemaining.toLong(), readable.remaining).toInt()
        readable.readFully(dst, size)
        afterRead(size)
        return size
    }

    internal suspend fun awaitInternalAtLeast1(): Boolean = if (readable.isNotEmpty) {
        true
    } else {
        awaitSuspend(1)
    }

    protected suspend fun awaitSuspend(atLeast: Int): Boolean {
        require(atLeast >= 0)

        awaitAtLeastNBytesAvailableForRead(atLeast)
        prepareFlushedBytes()

        closedCause?.let { throw it }
        return !isClosedForRead && availableForRead >= atLeast
    }

    override fun cancel(cause: Throwable?): Boolean {
        if (closedCause != null || closed) {
            return false
        }

        return close(cause ?: io.ktor.utils.io.CancellationException("Channel cancelled"))
    }

    override suspend fun awaitContent(predicate: AwaitPredicate) {
        TODO("Not yet implemented")
    }

    override fun close(cause: Throwable?): Boolean {
        val closeElement = if (cause == null) CLOSED_SUCCESS else CloseElement(cause)
        if (!_closed.compareAndSet(null, closeElement)) return false

        if (cause != null) {
            readable.release()
            writable.release()
            flushBuffer.release()
        } else {
            flushImpl()
        }

        slot.cancel(cause)
        return true
    }

    override fun write(buffer: ChunkBuffer) {
        TODO("Not yet implemented")
    }

    override fun writeBuffer(): ChunkBuffer {
        TODO("Not yet implemented")
    }

    internal fun transferTo(dst: ByteChannelSequentialBase, limit: Long): Long {
        val size = readable.remaining
        return if (size <= limit) {
            dst.writable.writePacket(readable)
            dst.afterWrite(size.toInt())
            afterRead(size.toInt())
            size
        } else {
            0
        }
    }

    private suspend fun writeAvailableSuspend(src: ChunkBuffer): Int {
        awaitAtLeastNBytesAvailableForWrite(1)
        return writeAvailable(src)
    }

    private suspend fun writeAvailableSuspend(src: ByteArray, offset: Int, length: Int): Int {
        awaitAtLeastNBytesAvailableForWrite(1)
        return writeAvailable(src, offset, length)
    }

    protected fun afterWrite(count: Int) {
        addBytesWritten(count)

        if (closed) {
            writable.release()
            ensureNotClosed()
        }
        if (autoFlush || availableForWrite == 0) {
            flushImpl()
        }
    }

    private fun addBytesRead(count: Int) {
        require(count >= 0) { "Can't read negative amount of bytes: $count" }

        channelSize.minusAssign(count)
        _totalBytesRead.addAndGet(count.toLong())
        _availableForRead.minusAssign(count)

        check(channelSize.value >= 0) { "Readable bytes count is negative: $availableForRead, $count in $this" }
        check(availableForRead >= 0) { "Readable bytes count is negative: $availableForRead, $count in $this" }
    }

    private fun addBytesWritten(count: Int) {
        require(count >= 0) { "Can't write negative amount of bytes: $count" }

        channelSize.plusAssign(count)
        _totalBytesWritten.addAndGet(count.toLong())

        check(channelSize.value >= 0) { "Readable bytes count is negative: ${channelSize.value}, $count in $this" }
    }
}
