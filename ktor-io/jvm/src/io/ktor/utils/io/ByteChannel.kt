package io.ktor.utils.io

import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import java.nio.*

/**
 * Creates channel for reading from the specified byte buffer.
 */
@OptIn(DelicateCoroutinesApi::class)
public fun ByteReadChannel(content: ByteBuffer): ByteReadChannel = GlobalScope.writer {
    channel.writeFully(content)
}.channel

/**
 * Creates buffered channel for asynchronous reading and writing of sequences of bytes.
 */
public actual fun ByteChannel(autoFlush: Boolean): ByteChannel = ByteChannelSequentialJVM(ChunkBuffer.Empty, autoFlush)

/**
 * Creates channel for reading from the specified byte array.
 */
public actual fun ByteReadChannel(content: ByteArray, offset: Int, length: Int): ByteReadChannel =
    ByteReadChannel(ByteBuffer.wrap(content, offset, length))

/**
 * Creates buffered channel for asynchronous reading and writing of sequences of bytes using [close] function to close
 * channel.
 */
public fun ByteChannel(autoFlush: Boolean = false, exceptionMapper: (Throwable?) -> Throwable?): ByteChannel {
    val delegate = ByteChannelSequentialJVM(ChunkBuffer.Empty, autoFlush)
    return object : ByteChannel by delegate {
        override fun close(cause: Throwable?): Boolean {
            val mappedException = exceptionMapper(cause)
            return delegate.close(mappedException)
        }
    }
}
