package io.ktor.utils.io

import io.ktor.utils.io.core.internal.*

/**
 * Channel for asynchronous reading of sequences of bytes.
 * This is a **single-reader channel**.
 *
 * Operations on this channel cannot be invoked concurrently.
 */
public interface ByteReadChannel {
    /**
     * Returns number of bytes that can be read without suspension. Read operations do no suspend and return
     * immediately when this number is at least the number of bytes requested for read.
     */
    public val availableForRead: Int

    /**
     * Returns `true` if the channel is closed and no remaining bytes are available for read.
     * It implies that [availableForRead] is zero.
     */
    public val isClosedForRead: Boolean

    /**
     * A closure causes exception or `null` if closed successfully or not yet closed
     */
    public val closedCause: Throwable?

    /**
     * Number of bytes read from the channel.
     * It is not guaranteed to be atomic so could be updated in the middle of long-running read operation.
     */
    public val totalBytesRead: Long

    public suspend fun peek(): ChunkBuffer

    public suspend fun read(buffer: ChunkBuffer)

    /**
     * Close channel with optional [cause] cancellation. Unlike [ByteWriteChannel.close] that could close channel
     * normally, cancel does always close with error so any operations on this channel will always fail
     * and all suspensions will be resumed with exception.
     *
     * Please note that if the channel has been provided by [reader] or [writer] then the corresponding owning
     * coroutine will be cancelled as well
     *
     * @see ByteWriteChannel.close
     */
    public fun cancel(cause: Throwable? = null): Boolean

    /**
     * Suspend until the channel has bytes to read or gets closed. Throws exception if the channel was closed with an error.
     */
    public suspend fun awaitContent(predicate: AwaitPredicate = AwaitPredicate.SomeBytes)

    public companion object {
        public val Empty: ByteReadChannel = object : ByteReadChannel {
            override val availableForRead: Int get() = 0
            override val isClosedForRead: Boolean get() = true
            override val closedCause: Throwable? get() = null
            override val totalBytesRead: Long get() = 0

            override suspend fun peek(): ChunkBuffer = ChunkBuffer.Empty
            override suspend fun read(buffer: ChunkBuffer) {}
            override fun cancel(cause: Throwable?): Boolean = false
            override suspend fun awaitContent(predicate: AwaitPredicate) {}
        }
    }
}

