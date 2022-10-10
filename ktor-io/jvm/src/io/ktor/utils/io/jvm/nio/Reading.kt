package io.ktor.utils.io.jvm.nio

import io.ktor.utils.io.*
import java.nio.channels.*

/**
 * Copies up to [limit] bytes from blocking NIO channel to CIO [ch]. It does suspend if no space available for writing
 * in the destination channel but may block if source NIO channel blocks.
 *
 * @return number of bytes were copied
 */
public suspend fun ReadableByteChannel.copyTo(ch: ByteWriteChannel, limit: Long = Long.MAX_VALUE): Long {
    TODO()
}

/**
 * Copies up to [limit] bytes from a blocking NIO pipe to CIO [ch]. A shortcut to copyTo with
 * NIO readable channel receiver
 *
 * @return number of bytes copied
 */
public suspend fun Pipe.copyTo(ch: ByteWriteChannel, limit: Long = Long.MAX_VALUE): Long = source().copyTo(ch, limit)
