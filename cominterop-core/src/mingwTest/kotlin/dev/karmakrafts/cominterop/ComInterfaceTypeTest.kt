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

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.IID
import kotlin.test.Test
import kotlin.test.assertEquals

class ComInterfaceTypeTest {
    private class Descriptor(
        override val functions: List<String>, override val superInterfaces: Array<ComInterfaceType> = arrayOf(IUnknown)
    ) : ComInterfaceType {
        override fun create(): ComInterface<*> = IUnknown()
        override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) = IUnknown.getIID(iid, iface)
    }

    @Test
    fun inheritedFunctionsPrecedeDerivedFunctions() {
        val parent = Descriptor(listOf("Parent"))
        val child = Descriptor(listOf("Child"), arrayOf(parent))
        assertEquals(listOf(IUnknown, parent), child.allSuperInterfaces())
        assertEquals(5, child.getTotalFunctionCount())
    }

    @Test
    fun sharedAncestorsAreCountedOnlyOnce() {
        val left = Descriptor(listOf("Left"))
        val right = Descriptor(listOf("Right"))
        val child = Descriptor(listOf("Child"), arrayOf(left, right))
        assertEquals(listOf(IUnknown, right, left), child.allSuperInterfaces())
        assertEquals(6, child.getTotalFunctionCount())
    }

    @Test
    fun rootDescriptorsHaveNoInheritedFunctions() {
        val root = Descriptor(emptyList(), emptyArray())
        assertEquals(emptyList(), root.allSuperInterfaces())
        assertEquals(0, root.getTotalFunctionCount())
    }
}