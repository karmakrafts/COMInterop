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

package dev.karmakrafts.cominterop.winrt.collections

import dev.karmakrafts.cominterop.assertRtDescriptor
import dev.karmakrafts.cominterop.vtable.VTableStruct
import dev.karmakrafts.cominterop.vtable.getVSelf
import dev.karmakrafts.cominterop.winrt.RtInt32
import dev.karmakrafts.cominterop.winrt.asRt
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import platform.posix.uint32_t
import platform.posix.uint32_tVar
import platform.windows.HRESULT
import platform.windows.S_OK
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val E_BOUNDS: HRESULT = 0x8000000B.toInt()

class IVectorViewTest {
    private class Vector(private val values: List<Int>) : VTableStruct(10) {
        init {
            vTable += List(6) { "Base$it" }
            vTable["GetAt"] = staticCFunction<COpaquePointer, uint32_t, COpaquePointer, HRESULT> { self, index, value ->
                val vector = self.getVSelf<Vector>()
                if (index >= vector.values.size.toUInt()) E_BOUNDS
                else {
                    value.reinterpret<IntVar>().pointed.value = vector.values[index.toInt()]
                    S_OK
                }
            }
            vTable["GetSize"] = staticCFunction<COpaquePointer, CPointer<uint32_tVar>, HRESULT> { self, size ->
                size.pointed.value = self.getVSelf<Vector>().values.size.toUInt()
                S_OK
            }
            vTable += listOf("IndexOf", "GetMany")
        }
    }

    @Test
    fun descriptorDefinesVectorViewAbi() {
        assertRtDescriptor(IVectorView, "IVectorView", listOf(RtInt32), 10)
        assertEquals("GetAt", IVectorView.functions[0])
        assertEquals("GetSize", IVectorView.functions[1])
    }

    @Test
    fun sizeIndicesAndSequenceReadTheNativeValues() {
        val expected = listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE)
        Vector(expected).use { implementation ->
            val vector: IVectorView = implementation.address.asRt(IVectorView, RtInt32)
            assertEquals(3, vector.size)
            assertEquals(0..2, vector.indices)
            assertEquals(expected, vector.asSequence<IntVar>().map { it.value }.toList())
            memScoped {
                val value = alloc<IntVar>()
                assertEquals(S_OK, vector.getAt(2, value.ptr))
                assertEquals(Int.MAX_VALUE, value.value)
                assertEquals(E_BOUNDS, vector.getAt(-1, value.ptr))
                assertEquals(E_BOUNDS, vector.getAt(3, value.ptr))
            }
        }
    }

    @Test
    fun emptyVectorHasNoIndicesOrSequenceElements() {
        Vector(emptyList()).use { implementation ->
            val vector: IVectorView = implementation.address.asRt(IVectorView, RtInt32)
            assertEquals(0, vector.size)
            assertTrue(vector.indices.isEmpty())
            assertTrue(vector.asSequence<IntVar>().none())
        }
    }
}