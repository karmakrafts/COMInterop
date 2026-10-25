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

import dev.karmakrafts.cominterop.ComTest
import dev.karmakrafts.cominterop.winrt.collections.IMap
import dev.karmakrafts.cominterop.winrt.collections.IPropertySet
import dev.karmakrafts.cominterop.winrt.collections.PropertySet
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class RtInterfaceTest : ComTest() {
    @Test
    fun queriesPreserveGenericArgumentsAndOwnTheirReference() {
        val instance: IPropertySet = PropertySet.activate(IPropertySet)
        val map: IMap = try {
            instance.asRt(IMap, RtString, IInspectable)
        }
        finally {
            instance.close()
        }
        map.use { theMap ->
            assertSame(IMap, theMap.interfaceType)
            assertEquals(listOf(RtString, IInspectable), theMap.instantiatedType.args)
            assertEquals(0, theMap.size)
            theMap.asRt(IPropertySet).use { props ->
                assertSame(IPropertySet, props.interfaceType)
            }
        }
    }

    @Test
    fun rawWrappingPreservesTheAddressAndGenericArguments() {
        val instance: IPropertySet = PropertySet.activate(IPropertySet)
        instance.use {
            val wrapper: IInspectable = it.address.asRt(IInspectable)
            assertEquals(it.address, wrapper.address)
            assertSame(IInspectable, wrapper.interfaceType)
            assertEquals(emptyList(), wrapper.instantiatedType.args)
        }
    }

    @Test
    fun wrongGenericArityFailsBeforeNativeDispatch() {
        val instance: IPropertySet = PropertySet.activate(IPropertySet)
        instance.use {
            assertFailsWith<IllegalStateException> { it.asRt<IMap, _>(IMap, RtString) }
            assertFailsWith<IllegalStateException> { it.address.asRt<IMap, _>(IMap) }
            assertFailsWith<IllegalArgumentException> { it.asRt<IMap, _>(IMap, RtString, RtString) }
        }
    }
}