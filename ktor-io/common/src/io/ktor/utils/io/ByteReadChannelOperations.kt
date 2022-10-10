/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.utils.io.core.*

/**
 * Reads a long number (suspending if not enough bytes available) or fails if channel has been closed
 * and not enough bytes.
 */
public suspend fun ByteReadChannel.readLong(): Long = TODO()

/**
 * Reads an int number (suspending if not enough bytes available) or fails if channel has been closed
 * and not enough bytes.
 */
public suspend fun ByteReadChannel.readInt(): Int = TODO()

/**
 * Reads a short number (suspending if not enough bytes available) or fails if channel has been closed
 * and not enough bytes.
 */
public suspend fun ByteReadChannel.readShort(): Short = TODO()

/**
 * Reads a byte (suspending if no bytes available yet) or fails if channel has been closed
 * and not enough bytes.
 */
public suspend fun ByteReadChannel.readByte(): Byte = TODO()

/**
 * Reads a boolean value (suspending if no bytes available yet) or fails if channel has been closed
 * and not enough bytes.
 */
public suspend fun ByteReadChannel.readBoolean(): Boolean = TODO()

/**
 * Reads double number (suspending if not enough bytes available) or fails if channel has been closed
 * and not enough bytes.
 */
public suspend fun ByteReadChannel.readDouble(): Double = TODO()

/**
 * Reads float number (suspending if not enough bytes available) or fails if channel has been closed
 * and not enough bytes.
 */
public suspend fun ByteReadChannel.readFloat(): Float = TODO()

/**
 * Discard up to [max] bytes
 *
 * @return number of bytes were discarded
 */
public suspend fun ByteReadChannel.discard(max: Long): Long = TODO()

/**
 * Reads exact [limit] bytes from [ByteReadChannel].
 */
public suspend fun ByteReadChannel.readPacket(limit: Long = Long.MAX_VALUE): ByteReadPacket = TODO()

/**
 * Read at most [limit] bytes from [ByteReadChannel] and return them as a [ByteReadPacket].
 */
public suspend fun ByteReadChannel.readRemaining(limit: Long = Long.MAX_VALUE): ByteReadPacket {
    TODO("Not yet implemented")
}

/**
 * Reads a line of UTF-8 characters to the specified [out] buffer up to [limit] characters.
 * Supports both CR-LF and LF line endings. No line ending characters will be appended to [out] buffer.
 * Throws an exception if the specified [limit] has been exceeded.
 *
 * @return `true` if line has been read (possibly empty) or `false` if channel has been closed
 * and no characters were read.
 */
public suspend fun <A : Appendable> ByteReadChannel.readUTF8LineTo(out: A, limit: Int): Boolean = TODO()

/**
 * Reads a line of UTF-8 characters up to [limit] characters.
 * Supports both CR-LF and LF line endings.
 * Throws an exception if the specified [limit] has been exceeded.
 *
 * @return a line string with no line endings or `null` of channel has been closed
 * and no characters were read.
 */
public suspend fun ByteReadChannel.readUTF8Line(limit: Int): String? = TODO()

/**
 * Discards all bytes in the channel and suspends until end of stream.
 */
public suspend fun ByteReadChannel.discard(): Long = discard(Long.MAX_VALUE)

/**
 * Discards exactly [n] bytes or fails if not enough bytes in the channel
 */
public suspend inline fun ByteReadChannel.discardExact(n: Long) {
    if (discard(n) != n) throw EOFException("Unable to discard $n bytes")
}

/**
 * Reads up to [limit] bytes from receiver channel and writes them to [dst] channel.
 * Closes [dst] channel if fails to read or write with cause exception.
 * @return a number of copied bytes
 */
public suspend fun ByteReadChannel.copyTo(dst: ByteWriteChannel, limit: Long = Long.MAX_VALUE): Long = TODO()

/**
 * Reads all the bytes from receiver channel and writes them to [dst] channel and then closes it.
 * Closes [dst] channel if fails to read or write with cause exception.
 * @return a number of copied bytes
 */
public suspend fun ByteReadChannel.copyAndClose(dst: ByteWriteChannel, limit: Long = Long.MAX_VALUE): Long {
    val count = copyTo(dst, limit)
    dst.close()
    return count
}
