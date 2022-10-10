/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*

/**
 * Writes as much as possible and only suspends if buffer is full
 */
public suspend fun ByteWriteChannel.writeAvailable(src: ByteArray, offset: Int, length: Int): Int { TODO() }

public suspend fun ByteWriteChannel.writeAvailable(src: ChunkBuffer): Int { TODO() }

/**
 * Writes all [src] bytes and suspends until all bytes written. Causes flush if buffer filled up or when [autoFlush]
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeFully(src: ByteArray, offset: Int, length: Int) { TODO() }

public suspend fun ByteWriteChannel.writeFully(src: Buffer) { TODO() }

public suspend fun ByteWriteChannel.writeFully(memory: Memory, startIndex: Int, endIndex: Int) { TODO() }

/**
 * Writes a [packet] fully or fails if channel get closed before the whole packet has been written
 */
public suspend fun ByteWriteChannel.writePacket(packet: ByteReadPacket) { TODO() }

/**
 * Writes long number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeLong(l: Long) { TODO() }

/**
 * Writes int number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeInt(i: Int) { TODO() }

/**
 * Writes short number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeShort(s: Short) { TODO() }

/**
 * Writes byte and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeByte(b: Byte) { TODO() }

/**
 * Writes double number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeDouble(d: Double) { TODO() }

/**
 * Writes float number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeFloat(f: Float) { TODO() }

public suspend fun ByteWriteChannel.writeAvailable(src: ByteArray): Int = writeAvailable(src, 0, src.size)

public suspend fun ByteWriteChannel.writeFully(src: ByteArray): Unit = writeFully(src, 0, src.size)

public suspend fun ByteWriteChannel.writeShort(s: Int) {
    return writeShort((s and 0xffff).toShort())
}

public suspend fun ByteWriteChannel.writeShort(s: Int, byteOrder: ByteOrder) {
    return writeShort((s and 0xffff).toShort(), byteOrder)
}

public suspend fun ByteWriteChannel.writeByte(b: Int) {
    return writeByte((b and 0xff).toByte())
}

public suspend fun ByteWriteChannel.writeInt(i: Long) {
    return writeInt(i.toInt())
}

public suspend fun ByteWriteChannel.writeInt(i: Long, byteOrder: ByteOrder) {
    return writeInt(i.toInt(), byteOrder)
}

/**
 * Closes this channel with no failure (successfully)
 */
public fun ByteWriteChannel.close(): Boolean = close(null)

public suspend fun ByteWriteChannel.writeStringUtf8(s: CharSequence) {
    val packet = buildPacket {
        writeText(s)
    }

    return writePacket(packet)
}

public suspend fun ByteWriteChannel.writeStringUtf8(s: String) {
    val packet = buildPacket {
        writeText(s)
    }

    return writePacket(packet)
}

public suspend fun ByteWriteChannel.writeBoolean(b: Boolean) {
    return writeByte(if (b) 1 else 0)
}

/**
 * Writes UTF16 character
 */
public suspend fun ByteWriteChannel.writeChar(ch: Char) {
    return writeShort(ch.code)
}

public suspend inline fun ByteWriteChannel.writePacket(builder: BytePacketBuilder.() -> Unit) {
    return writePacket(buildPacket(builder))
}

public suspend fun ByteWriteChannel.writePacketSuspend(builder: suspend BytePacketBuilder.() -> Unit) {
    return writePacket(buildPacket { builder() })
}

/**
 * Indicates attempt to write on [isClosedForWrite][ByteWriteChannel.isClosedForWrite] channel
 * that was closed without a cause. A _failed_ channel rethrows the original [close][ByteWriteChannel.close] cause
 * exception on send attempts.
 */
public class ClosedWriteChannelException(message: String?) : CancellationException(message)
