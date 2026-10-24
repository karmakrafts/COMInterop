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

package dev.karmakrafts.cominterop

import dev.karmakrafts.cominterop.win32.IDispatch
import dev.karmakrafts.cominterop.winrt.IInspectable
import dev.karmakrafts.cominterop.winrt.collections.IPropertySet
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.IID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ComInterfaceTypeTest {
    @Test
    fun unknownIsTheRootInterface() {
        assertTrue(IUnknown.superInterfaces.isEmpty())
        assertTrue(IUnknown.allSuperInterfaces().isEmpty())
        assertEquals(listOf("QueryInterface", "AddRef", "Release"), IUnknown.functions)
        assertEquals(3, IUnknown.getTotalFunctionCount())
    }

    @Test
    fun dispatchInheritsUnknownByDefault() {
        assertEquals(listOf(IUnknown), IDispatch.superInterfaces.toList())
        assertEquals(listOf(IUnknown), IDispatch.allSuperInterfaces())
        assertEquals(7, IDispatch.getTotalFunctionCount())
    }

    @Test
    fun winrtInheritanceListsBaseInterfacesBeforeDerivedInterfaces() {
        assertEquals(listOf(IUnknown), IInspectable.allSuperInterfaces())
        assertEquals(listOf(IUnknown, IInspectable), IPropertySet.allSuperInterfaces())
        assertEquals(6, IInspectable.getTotalFunctionCount())
        assertEquals(6, IPropertySet.getTotalFunctionCount())
    }

    @Test
    fun factoriesCreateIndependentWrappersWithTheirDescriptor() {
        for (type in listOf(IUnknown, IDispatch, IInspectable, IPropertySet)) {
            val first = type.create()
            val second = type.create()
            assertNotSame(first, second)
            assertSame(type, first.interfaceType)
            assertSame(type, second.interfaceType)
        }
        assertIs<IUnknown>(IUnknown.create())
        assertIs<IDispatch>(IDispatch.create())
        assertIs<IInspectable>(IInspectable.create())
        assertIs<IPropertySet>(IPropertySet.create())
    }

    @Test
    fun descriptorsExposeStandardInterfaceIds() = memScoped {
        val iid = alloc<IID>()
        val types = listOf(
            IUnknown to "{00000000-0000-0000-C000-000000000046}",
            IDispatch to "{00020400-0000-0000-C000-000000000046}",
            IInspectable to "{AF86E2E0-B12D-4C6A-9C5A-D7AA65101E90}",
            IPropertySet to "{8A43ED9F-F4E6-4421-ACF9-1DAB2986820C}"
        )
        for ([type, expected] in types) {
            type.getIID(iid.ptr, type.create())
            assertEquals(expected, ComRuntime.guidToString(iid))
        }
    }
}