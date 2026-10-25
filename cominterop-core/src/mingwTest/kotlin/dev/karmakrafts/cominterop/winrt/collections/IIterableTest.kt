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
import dev.karmakrafts.cominterop.assertRtDescriptor
import dev.karmakrafts.cominterop.winrt.RtString
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.S_OK
import platform.windows.booleanVar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class IIterableTest : ComTest() {
    @Test
    fun descriptorDefinesOneElementArgumentAndFirst() {
        assertRtDescriptor(IIterable, "IIterable", listOf(RtString), 7)
        assertEquals(listOf("First"), IIterable.functions)
    }

    @Test
    fun firstReturnsAnIteratorWithTheSameElementType() = withStringMap { map ->
        val element = IKeyValuePair[RtString, RtString]
        val iterable: IIterable = map.asRt(IIterable, element)
        iterable.use {
            it.first.use { iterator ->
                assertSame(IIterator, iterator.interfaceType)
                assertEquals(listOf(element), iterator.instantiatedType.args)
                memScoped {
                    val hasCurrent = alloc<booleanVar>()
                    assertEquals(S_OK, iterator.GetHasCurrent(iterator.address, hasCurrent.ptr))
                    assertEquals(0.toUByte(), hasCurrent.value)
                }
            }
        }
    }
}