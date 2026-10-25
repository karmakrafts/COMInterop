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

import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.IID
import platform.windows.E_NOINTERFACE
import platform.windows.S_OK
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IUnknownImplTest {
    @Test
    fun implementationRejectsReferencesAfterFinalRelease() {
        val implementation = TestUnknown()
        val address = implementation.address
        assertEquals(0U, implementation.release(address))
        assertTrue(implementation.isClosed)
        assertFailsWith<IllegalStateException> { implementation.addRef(address) }
        assertFailsWith<IllegalStateException> { implementation.release(address) }
    }

    @Test
    fun implementationRecognizesUnknownAndRejectsOtherInterfaces() = memScoped {
        val implementation = object : IUnknownImpl(0) {}
        try {
            val iid = alloc<IID>()
            val result = alloc<COpaquePointerVar> { value = null }
            IUnknown.getIID(iid.ptr, IUnknown())
            assertEquals(S_OK, implementation.queryInterface(implementation.address, iid.ptr, result.ptr))
            assertEquals(implementation.address, result.value)
            ComRuntime.iidFromString("{00020400-0000-0000-C000-000000000046}", iid.ptr)
            assertEquals(E_NOINTERFACE, implementation.queryInterface(implementation.address, iid.ptr, result.ptr))
            assertNull(result.value)
        }
        finally {
            implementation.release(implementation.address)
        }
    }
}