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
import dev.karmakrafts.cominterop.vtable.VTableStruct
import dev.karmakrafts.cominterop.vtable.getVSelf
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf16
import platform.posix.GUID
import platform.windows.E_INVALIDARG
import platform.windows.HRESULT
import platform.windows.PCWSTR
import platform.windows.PCWSTRVar
import platform.windows.S_OK
import platform.windows.UINT32
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IRoSimpleMetaDataBuilderTest {
    private class Builder : VTableStruct(10) {
        var iid: String? = null
        var name: String? = null
        var count: UInt? = null
        var fields: CPointer<PCWSTRVar>? = null
        var result: HRESULT = S_OK

        init {
            vTable["SetWinRtInterface"] = staticCFunction<COpaquePointer, CPointer<GUID>, HRESULT> { self, iid ->
                val builder = self.getVSelf<Builder>()
                builder.iid = ComRuntime.guidToString(iid.pointed)
                builder.result
            }
            vTable += List(5) { "Reserved${it + 1}" }
            vTable["SetStruct"] = staticCFunction<COpaquePointer, PCWSTR, UINT32, CPointer<PCWSTRVar>?, HRESULT> { self, name, count, fields ->
                val builder = self.getVSelf<Builder>()
                builder.name = name.toKStringFromUtf16()
                builder.count = count
                builder.fields = fields
                builder.result
            }
            vTable += "Reserved7"
            vTable["SetParametrizedInterface"] = staticCFunction<COpaquePointer, CPointer<GUID>, UINT32, HRESULT> { self, iid, count ->
                val builder = self.getVSelf<Builder>()
                builder.iid = ComRuntime.guidToString(iid.pointed)
                builder.count = count
                builder.result
            }
            vTable += "Reserved9"
        }
    }

    @Test
    fun primitiveUsesTheStructSlotWithNoFields() {
        Builder().use { implementation ->
            val builder = IRoSimpleMetaDataBuilder(implementation.address)
            assertEquals(S_OK, builder.setPrimitive("Int32"))
            assertEquals("Int32", implementation.name)
            assertEquals(0U, implementation.count)
            assertNull(implementation.fields)
            implementation.result = E_INVALIDARG
            assertEquals(E_INVALIDARG, builder.setPrimitive("Invalid"))
        }
    }

    @Test
    fun interfaceSettersForwardGuidArityAndNativeResult() = memScoped {
        val expected = "{FAA585EA-6214-4217-AFDA-7F46DE5869B3}"
        val iid = alloc<GUID>()
        ComRuntime.iidFromString(expected, iid.ptr)
        Builder().use { implementation ->
            val builder = IRoSimpleMetaDataBuilder(implementation.address)
            assertEquals(S_OK, builder.setWinRtInterface(iid))
            assertEquals(expected, implementation.iid)
            assertEquals(S_OK, builder.setParametrizedInterface(iid, 2U))
            assertEquals(expected, implementation.iid)
            assertEquals(2U, implementation.count)
            implementation.result = E_INVALIDARG
            assertEquals(E_INVALIDARG, builder.setWinRtInterface(iid))
            assertEquals(E_INVALIDARG, builder.setParametrizedInterface(iid, 0U))
            assertEquals(0U, implementation.count)
        }
    }
}