package io.ktor.utils.io

import io.ktor.utils.io.bits.*
import java.nio.*

/**
 * Visitor function that is invoked for every available buffer (or chunk) of a channel.
 * The last parameter shows that the buffer is known to be the last.
 */
public typealias ConsumeEachBufferVisitor = (buffer: ByteBuffer, last: Boolean) -> Boolean

/**
 * For every available bytes range invokes [visitor] function until it return false or end of stream encountered.
 * The provided buffer should be never captured outside of the visitor block otherwise resource leaks, crashes and
 * data corruptions may occur. The visitor block may be invoked multiple times, once or never.
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public suspend inline fun ByteReadChannel.consumeEachBufferRange(visitor: ConsumeEachBufferVisitor) {
    TODO()
}
