package io.ktor.utils.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.Buffer
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.internal.*
import io.ktor.utils.io.pool.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.io.EOFException
import java.lang.Double.*
import java.lang.Float.*
import java.nio.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

internal const val DEFAULT_CLOSE_MESSAGE: String = "Byte channel was closed"
private const val BYTE_BUFFER_CAPACITY: Int = 4088

private fun rethrowClosed(cause: Throwable): Nothing {
    val clone = try {
        tryCopyException(cause, cause)
    } catch (_: Throwable) {
        null
    }

    throw clone ?: cause
}
