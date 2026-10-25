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
import dev.karmakrafts.cominterop.winrt.IInspectable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals

class StringMapTest : ComTest() {
    @Test
    fun activationReportsTheStringMapRuntimeName() {
        assertEquals("Windows.Foundation.Collections.StringMap", StringMap.rtTypeName)
        withStringMap { map ->
            map.asCom<IInspectable, _>(IInspectable).use { assertEquals(StringMap.rtTypeName, it.runtimeClassName) }
        }
    }

    @Test
    fun activationsDoNotShareTheirContents() {
        withStringMap("key" to "value") { first ->
            withStringMap { second ->
                assertEquals(1, first.size)
                assertEquals(0, second.size)
            }
        }
    }
}