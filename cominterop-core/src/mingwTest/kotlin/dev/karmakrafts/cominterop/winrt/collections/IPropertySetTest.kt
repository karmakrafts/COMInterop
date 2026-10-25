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
import dev.karmakrafts.cominterop.assertComDescriptor
import dev.karmakrafts.cominterop.winrt.IInspectable
import dev.karmakrafts.cominterop.winrt.activate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class IPropertySetTest : ComTest() {
    @Test
    fun markerInterfaceInheritsOnlyInspectableFunctions() {
        assertEquals(listOf(IInspectable), IPropertySet.superInterfaces.toList())
        assertEquals(listOf(IUnknown, IInspectable), IPropertySet.allSuperInterfaces())
        assertEquals(emptyList(), IPropertySet.functions)
        assertIs<IPropertySet>(IPropertySet.create())
        assertComDescriptor(IPropertySet, "{8A43ED9F-F4E6-4421-ACF9-1DAB2986820C}", 6)
    }

    @Test
    fun propertySetSupportsTheMarkerInterface() {
        val instance: IPropertySet = PropertySet.activate(IPropertySet)
        instance.use { set ->
            set.asRt(IPropertySet).use { iface ->
                assertSame(IPropertySet, iface.interfaceType)
            }
        }
    }
}