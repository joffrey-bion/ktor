package io.ktor.utils.io

import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import java.nio.*

/**
 * Creates channel for reading from the specified byte buffer.
 */
@OptIn(DelicateCoroutinesApi::class)
public fun ByteReadChannel(content: ByteBuffer): ByteReadChannel = GlobalScope.writer {
    TODO()
}.channel

/**
 * Creates buffered channel for asynchronous reading and writing of sequences of bytes using [close] function to close
 * channel.
 */
public fun ByteChannel(autoFlush: Boolean = false, exceptionMapper: (Throwable?) -> Throwable?): ByteChannel {
    val delegate = ByteChannelSequentialBase(ChunkBuffer.Empty, autoFlush)
    return object : ByteChannel by delegate {
        override fun close(cause: Throwable?): Boolean {
            val mappedException = exceptionMapper(cause)
            return delegate.close(mappedException)
        }
    }
}
