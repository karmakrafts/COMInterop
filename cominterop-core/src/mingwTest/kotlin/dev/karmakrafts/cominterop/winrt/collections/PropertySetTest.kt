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
import dev.karmakrafts.cominterop.winrt.RtString
import dev.karmakrafts.cominterop.winrt.activate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PropertySetTest : ComTest() {
    @Test
    fun activationCreatesIndependentEmptyPropertySets() {
        assertEquals("Windows.Foundation.Collections.PropertySet", PropertySet.rtTypeName)
        val first: IPropertySet = PropertySet.activate(IPropertySet)
        first.use {
            val second: IPropertySet = PropertySet.activate(IPropertySet)
            second.use {
                assertNotEquals(first.address, second.address)
                for (instance in listOf(first, second)) {
                    instance.asCom<IInspectable, _>(IInspectable).use {
                        assertEquals(PropertySet.rtTypeName, it.runtimeClassName)
                    }
                    val map: IMap = instance.asRt(IMap, RtString, IInspectable)
                    map.use { assertEquals(0, it.size) }
                }
            }
        }
    }
}