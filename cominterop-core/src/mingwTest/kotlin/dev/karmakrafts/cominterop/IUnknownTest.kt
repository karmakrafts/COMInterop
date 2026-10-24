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

import dev.karmakrafts.cominterop.vtable.getVSelf
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.IID
import platform.windows.E_NOINTERFACE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class IUnknownTest {
    private class Implementation : IUnknownImpl(0) {
        var isClosed: Boolean = false
            private set

        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
        override fun close() {
            isClosed = true
            super.close()
        }
    }

    @Test
    fun rawPointerWrappingPreservesAddressAndImplementation() {
        val implementation = Implementation()
        try {
            val wrapper: IUnknown = implementation.address.asCom(IUnknown)
            assertEquals(implementation.address, wrapper.address)
            assertSame(IUnknown, wrapper.interfaceType)
            assertSame(implementation, wrapper.address.getVSelf<Implementation>())
        }
        finally {
            implementation.release(implementation.address)
        }
    }

    @Test
    fun wrapperDispatchesReferenceCountingThroughTheNativeVTable() {
        val implementation = Implementation()
        val wrapper: IUnknown = implementation.address.asCom(IUnknown)
        try {
            assertEquals(2U, wrapper.addRef())
            try {
                assertFalse(implementation.isClosed)
            }
            finally {
                assertEquals(1U, wrapper.release())
            }
            assertFalse(implementation.isClosed)
        }
        finally {
            assertEquals(0U, wrapper.release())
        }
        assertTrue(implementation.isClosed)
    }

    @Test
    fun closingAWrapperReleasesItsReference() {
        val implementation = Implementation()
        val wrapper: IUnknown = implementation.address.asCom(IUnknown)
        wrapper.close()
        assertTrue(implementation.isClosed)
    }

    @Test
    fun useReleasesTheReferenceWhenTheBlockThrows() {
        val implementation = Implementation()
        val wrapper: IUnknown = implementation.address.asCom(IUnknown)
        assertFailsWith<IllegalStateException> {
            wrapper.use { error("test failure") }
        }
        assertTrue(implementation.isClosed)
    }

    @Test
    fun unsupportedInterfaceQueryClearsTheOutputPointer() = memScoped {
        val implementation = Implementation()
        val wrapper: IUnknown = implementation.address.asCom(IUnknown)
        try {
            val iid = alloc<IID>()
            ComRuntime.iidFromString("{00020400-0000-0000-C000-000000000046}", iid.ptr)
            val result = alloc<COpaquePointerVar> { value = implementation.address }
            assertEquals(E_NOINTERFACE, wrapper.queryInterface(iid.ptr, result.ptr))
            assertNull(result.value)
            assertFalse(implementation.isClosed)
        }
        finally {
            wrapper.close()
        }
    }

    @Test
    fun implementationRejectsReferencesAfterFinalRelease() {
        val implementation = Implementation()
        val address = implementation.address
        assertEquals(0U, implementation.release(address))
        assertTrue(implementation.isClosed)
        assertFailsWith<IllegalStateException> { implementation.addRef(address) }
        assertFailsWith<IllegalStateException> { implementation.release(address) }
    }

    @Test
    fun uninitializedWrapperCannotCallNativeMethods() {
        val failure = assertFailsWith<RuntimeException> { IUnknown().addRef() }
        assertEquals("lateinit property address has not been initialized", failure.message)
    }
}