package io.ktor.utils.io.jvm.nio

import io.ktor.utils.io.*
import java.nio.channels.*

/**
 * Copy up to [limit] bytes to blocking NIO [channel]. Copying to non-blocking channel requires selection and
 * not supported. It does suspend if no data available in byte channel but may block if destination NIO channel blocks.
 *
 * @return number of bytes copied
 */
public suspend fun ByteReadChannel.copyTo(channel: WritableByteChannel, limit: Long = Long.MAX_VALUE): Long {
    TODO()
}

/**
 * Copy up to [limit] bytes to blocking [pipe]. A shortcut to copyTo function with NIO channel destination
 *
 * @return number of bytes were copied
 */
public suspend fun ByteReadChannel.copyTo(pipe: Pipe, limit: Long = Long.MAX_VALUE): Long = copyTo(pipe.sink(), limit)
