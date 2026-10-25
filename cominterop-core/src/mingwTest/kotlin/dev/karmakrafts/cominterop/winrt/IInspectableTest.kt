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
import dev.karmakrafts.cominterop.IUnknown
import dev.karmakrafts.cominterop.assertComDescriptor
import dev.karmakrafts.cominterop.winrt.collections.IPropertySet
import dev.karmakrafts.cominterop.winrt.collections.PropertySet
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class IInspectableTest : ComTest() {
    @Test
    fun descriptorDefinesTheInspectableAbi() {
        assertEquals(listOf(IUnknown), IInspectable.superInterfaces.toList())
        assertEquals(listOf(IUnknown), IInspectable.allSuperInterfaces())
        assertEquals(listOf("GetIids", "GetRuntimeClassName", "GetTrustLevel"), IInspectable.functions)
        assertEquals("Object", IInspectable.rtTypeName)
        assertTrue(IInspectable.isInstantiated)
        assertIs<IInspectable>(IInspectable.create())
        assertComDescriptor(IInspectable, "{AF86E2E0-B12D-4C6A-9C5A-D7AA65101E90}", 6)
    }

    @Test
    fun activatedObjectReportsItsRuntimeClassAndInterfaces() {
        val instance: IPropertySet = PropertySet.activate(IPropertySet)
        instance.use { set ->
            set.asRt<IInspectable, _>(IInspectable).use { inspectable ->
                assertEquals(PropertySet.rtTypeName, inspectable.runtimeClassName)
                val iids = inspectable.getIIds().map(ComRuntime::guidToString)
                assertTrue("{8A43ED9F-F4E6-4421-ACF9-1DAB2986820C}" in iids)
                assertEquals(iids.size, iids.distinct().size)
            }
        }
    }
}