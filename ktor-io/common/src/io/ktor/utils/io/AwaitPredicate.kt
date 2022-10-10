/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

public fun interface AwaitPredicate {

    public fun condition(channel: ByteReadChannel): Boolean

    public companion object {
        public val SomeBytes: AwaitPredicate = AwaitPredicate { channel -> channel.availableForRead > 0 }
    }
}
