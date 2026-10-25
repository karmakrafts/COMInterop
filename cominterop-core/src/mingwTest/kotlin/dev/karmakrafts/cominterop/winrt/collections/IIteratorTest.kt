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

import dev.karmakrafts.cominterop.ComTest
import dev.karmakrafts.cominterop.IUnknown
import dev.karmakrafts.cominterop.asCom
import dev.karmakrafts.cominterop.assertRtDescriptor
import dev.karmakrafts.cominterop.winrt.RtString
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.uint32_tVar
import platform.windows.S_OK
import platform.windows.booleanVar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IIteratorTest : ComTest() {
    @Test
    fun descriptorDefinesIteratorAbi() {
        assertRtDescriptor(IIterator, "IIterator", listOf(RtString), 10)
        assertEquals(listOf("GetCurrent", "GetHasCurrent", "MoveNext", "GetMany"), IIterator.functions)
    }

    @Test
    fun iteratorReadsTheCurrentPairAndAdvancesToTheEnd() = withStringMap("key" to "value") { map ->
        val iterable: IIterable = map.asRt(IIterable, IKeyValuePair[RtString, RtString])
        iterable.use {
            it.first.use { iterator ->
                memScoped {
                    val hasCurrent = alloc<booleanVar>()
                    assertEquals(S_OK, iterator.GetHasCurrent(iterator.address, hasCurrent.ptr))
                    assertEquals(1.toUByte(), hasCurrent.value)
                    val current = alloc<COpaquePointerVar> { value = null }
                    assertEquals(S_OK, iterator.GetCurrent(iterator.address, current.ptr))
                    assertNotNull(current.value).asCom<IUnknown, _>(IUnknown).close()
                    assertEquals(S_OK, iterator.MoveNext(iterator.address, hasCurrent.ptr))
                    assertEquals(0.toUByte(), hasCurrent.value)
                    assertEquals(S_OK, iterator.GetHasCurrent(iterator.address, hasCurrent.ptr))
                    assertEquals(0.toUByte(), hasCurrent.value)
                }
            }
        }
    }

    @Test
    fun bulkReadReturnsOnlyAvailableElements() = withStringMap("one" to "1", "two" to "2") { map ->
        val iterable: IIterable = map.asRt(IIterable, IKeyValuePair[RtString, RtString])
        iterable.use {
            it.first.use { iterator ->
                memScoped {
                    val values = allocArray<COpaquePointerVar>(3)
                    val actual = alloc<uint32_tVar>()
                    assertEquals(S_OK, iterator.GetMany(iterator.address, 3U, values, actual.ptr))
                    try {
                        assertEquals(2U, actual.value)
                        for (index in 0..<actual.value.toInt()) assertNotNull(values[index])
                    }
                    finally {
                        for (index in 0..<actual.value.toInt()) {
                            values[index]?.asCom<IUnknown, _>(IUnknown)?.close()
                        }
                    }
                    assertEquals(S_OK, iterator.GetMany(iterator.address, 3U, values, actual.ptr))
                    assertEquals(0U, actual.value)
                }
            }
        }
    }
}