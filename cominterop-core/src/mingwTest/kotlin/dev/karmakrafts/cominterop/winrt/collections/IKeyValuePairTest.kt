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

import dev.karmakrafts.cominterop.ComRuntime
import dev.karmakrafts.cominterop.ComTest
import dev.karmakrafts.cominterop.HSTRINGVar
import dev.karmakrafts.cominterop.assertRtDescriptor
import dev.karmakrafts.cominterop.winrt.RtString
import dev.karmakrafts.cominterop.winrt.asRt
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.S_OK
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IKeyValuePairTest : ComTest() {
    @Test
    fun descriptorDefinesKeyAndValueGetters() {
        assertRtDescriptor(IKeyValuePair, "IKeyValuePair", listOf(RtString, RtString), 8)
        assertEquals(listOf("GetKey", "GetValue"), IKeyValuePair.functions)
    }

    @Test
    fun pairReturnsTheInsertedKeyAndValue() = withStringMap("日本語" to "Grüße") { map ->
        val iterable: IIterable = map.asRt(IIterable, IKeyValuePair[RtString, RtString])
        iterable.use {
            it.first.use { iterator ->
                memScoped {
                    val current = alloc<COpaquePointerVar> { value = null }
                    assertEquals(S_OK, iterator.GetCurrent(iterator.address, current.ptr))
                    assertNotNull(current.value).asRt<IKeyValuePair, _>(IKeyValuePair, RtString, RtString).use { pair ->
                        val key = alloc<HSTRINGVar> { value = null }
                        val value = alloc<HSTRINGVar> { value = null }
                        try {
                            assertEquals(S_OK, pair.getKey(key.ptr))
                            assertEquals(S_OK, pair.getValue(value.ptr))
                            assertEquals("日本語", ComRuntime.getString(assertNotNull(key.value)))
                            assertEquals("Grüße", ComRuntime.getString(assertNotNull(value.value)))
                        }
                        finally {
                            key.value?.let(ComRuntime::deleteString)
                            value.value?.let(ComRuntime::deleteString)
                        }
                    }
                }
            }
        }
    }
}