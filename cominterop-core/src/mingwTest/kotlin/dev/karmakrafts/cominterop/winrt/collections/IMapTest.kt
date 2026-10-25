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
import dev.karmakrafts.cominterop.hstring
import dev.karmakrafts.cominterop.winrt.RtString
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IMapTest : ComTest() {
    @Test
    fun descriptorDefinesKeyValueArgumentsAndAbiSlots() {
        assertRtDescriptor(IMap, "IMap", listOf(RtString, RtString), 13)
        assertEquals("Lookup", IMap.functions[0])
        assertEquals("GetSize", IMap.functions[1])
        assertEquals("Insert", IMap.functions[4])
    }

    @Test
    fun insertAndReplaceRoundTripUnicodeValues() = withStringMap { map ->
        memScoped {
            assertEquals(0, map.size)
            assertTrue(map.indices.isEmpty())
            val key = hstring("日本語 \uD83D\uDE80")
            for (text in listOf("first", "Grüße")) {
                assertTrue(map.insert(key, hstring(text)))
                assertEquals(1, map.size)
                assertEquals(0..0, map.indices)
                val value = alloc<HSTRINGVar> { value = null }
                assertTrue(map.lookup(key, value.ptr))
                val handle = assertNotNull(value.value)
                try {
                    assertEquals(text, ComRuntime.getString(handle))
                }
                finally {
                    ComRuntime.deleteString(handle)
                }
            }
        }
    }

    @Test
    fun missingKeyDoesNotReturnAValue() = withStringMap { map ->
        memScoped {
            val value = alloc<HSTRINGVar> { value = null }
            assertFalse(map.lookup(hstring("missing"), value.ptr))
            assertNull(value.value)
            assertEquals(0, map.size)
        }
    }
}