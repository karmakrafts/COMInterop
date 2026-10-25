/*
 * Copyright 2026 Karma Krafts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalForeignApi::class)

package dev.karmakrafts.cominterop.winrt

import dev.karmakrafts.cominterop.ComRuntime
import dev.karmakrafts.cominterop.ComTest
import dev.karmakrafts.cominterop.winrt.collections.IIterable
import dev.karmakrafts.cominterop.winrt.collections.IIterator
import dev.karmakrafts.cominterop.winrt.collections.IKeyValuePair
import dev.karmakrafts.cominterop.winrt.collections.IMap
import dev.karmakrafts.cominterop.winrt.collections.IVectorView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.IID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class RtInterfaceIdResolverTest : ComTest() {
    @Test
    fun resolvesStandardParameterizedInterfaceIds() = memScoped {
        val types = listOf(
            IIterable[RtString] to "{E2FCC7C1-3BFC-5A0B-B2B0-72E769D1CB7E}",
            IIterator[RtString] to "{8C304EBB-6615-50A4-8829-879ECD443236}",
            IKeyValuePair[RtString, RtString] to "{60310303-49C5-52E6-ABC6-A9B36ECCC716}",
            IMap[RtString, RtString] to "{F6D1F700-49C2-52AE-8154-826F9908773C}",
            IVectorView[RtString] to "{2F13C006-A03A-5F69-B090-75A43E33423E}"
        )
        val iid = alloc<IID>()
        for ([type, expected] in types) {
            repeat(2) {
                RtInterfaceIdResolver.resolve(type, iid.ptr)
                assertEquals(expected, ComRuntime.guidToString(iid), type.rtTypeName)
            }
        }
    }

    @Test
    fun distinctGenericArgumentsProduceDistinctIds() = memScoped {
        val first = alloc<IID>()
        val second = alloc<IID>()
        RtInterfaceIdResolver.resolve(IIterable[RtString], first.ptr)
        RtInterfaceIdResolver.resolve(IIterable[RtInt32], second.ptr)
        assertFalse(ComRuntime.iidEquals(first.ptr, second.ptr))
    }

    @Test
    fun unresolvedGenericArgumentsAreRejected(): Unit = memScoped {
        val iid = alloc<IID>()
        assertFailsWith<IllegalArgumentException> { RtInterfaceIdResolver.resolve(IIterable[IMap], iid.ptr) }
    }
}