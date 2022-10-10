package io.ktor.utils.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*

/**
 * Await for [desiredSpace] will be available for write and invoke [block] function providing [Memory] instance and
 * the corresponding range suitable for wiring in the memory. The block function should return number of bytes were
 * written, possibly 0.
 *
 * Similar to [ByteReadChannel.read], this function may invoke block function with lesser memory range when the
 * specified [desiredSpace] is bigger that the buffer's capacity
 * or when it is impossible to represent all [desiredSpace] bytes as a single memory range
 * due to internal implementation reasons.
 */
public suspend inline fun ByteWriteChannel.write(
    desiredSpace: Int = 1,
    block: (freeSpace: Memory, startOffset: Long, endExclusive: Long) -> Int
): Int {
    val buffer = requestWriteBuffer(desiredSpace) ?: Buffer.Empty
    var bytesWritten = 0
    try {
        bytesWritten = block(buffer.memory, buffer.writePosition.toLong(), buffer.limit.toLong())
        buffer.commitWritten(bytesWritten)
        return bytesWritten
    } finally {
        completeWriting(buffer, bytesWritten)
    }
}

@PublishedApi
internal suspend fun requestWriteBuffer(desiredSpace: Int): Buffer? {
    return writeBufferFallback()
}

@PublishedApi
internal suspend fun ByteWriteChannel.completeWriting(buffer: Buffer, written: Int) {
    return completeWritingFallback(buffer)
}

@Suppress("DEPRECATION")
private suspend fun ByteWriteChannel.completeWritingFallback(buffer: Buffer) {
    if (buffer is ChunkBuffer) {
        writeFully(buffer)
        buffer.release(ChunkBuffer.Pool)
        return
    }

    throw UnsupportedOperationException("Only ChunkBuffer instance is supported.")
}

private fun writeBufferFallback(): Buffer = ChunkBuffer.Pool.borrow().also {
    it.resetForWrite()
    it.reserveEndGap(Buffer.ReservedSize)
}
