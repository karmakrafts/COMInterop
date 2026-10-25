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
import dev.karmakrafts.cominterop.win32.IDispatch
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ComInterfaceTest : ComTest() {
    @Test
    fun rawPointerWrappingPreservesAddressAndImplementation() {
        val implementation = TestUnknown()
        try {
            val wrapper: IUnknown = implementation.address.asCom(IUnknown)
            assertEquals(implementation.address, wrapper.address)
            assertSame(IUnknown, wrapper.interfaceType)
            assertSame(implementation, wrapper.address.getVSelf<TestUnknown>())
        }
        finally {
            implementation.release(implementation.address)
        }
    }

    @Test
    fun wrapperDispatchesReferenceCountingThroughTheNativeVTable() {
        val implementation = TestUnknown()
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
        val implementation = TestUnknown()
        val wrapper: IUnknown = implementation.address.asCom(IUnknown)
        wrapper.close()
        assertTrue(implementation.isClosed)
    }

    @Test
    fun useReleasesTheReferenceWhenTheBlockThrows() {
        val implementation = TestUnknown()
        val wrapper: IUnknown = implementation.address.asCom(IUnknown)
        assertFailsWith<IllegalStateException> {
            wrapper.use { error("test failure") }
        }
        assertTrue(implementation.isClosed)
    }

    @Test
    fun unsupportedInterfaceQueryClearsTheOutputPointer() = memScoped {
        val implementation = TestUnknown()
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
    fun uninitializedWrapperCannotCallNativeMethods() {
        val failure = assertFailsWith<RuntimeException> { IUnknown().addRef() }
        assertEquals("lateinit property address has not been initialized", failure.message)
    }

    @Test
    fun queryingUnknownPreservesComIdentityAndAcquiresAReference() {
        val instance: IUnknown = ShellLink.new()
        val queried: IUnknown = try {
            instance.asCom(IUnknown)
        }
        finally {
            instance.close()
        }
        queried.use { unknown ->
            assertEquals(instance.address, unknown.address)
            unknown.asCom(IUnknown).use { theUnknown ->
                assertEquals(queried.address, theUnknown.address)
            }
        }
    }

    @Test
    fun rawQueryReturnsAnOwnedUnknownInterface() = memScoped {
        val instance: IUnknown = ShellLink.new()
        instance.use {
            val iid = alloc<IID>()
            IUnknown.getIID(iid.ptr, instance)
            val result = alloc<COpaquePointerVar> { value = null }
            assertEquals(S_OK, instance.queryInterface(iid.ptr, result.ptr))
            val queried: IUnknown = assertNotNull(result.value).asCom(IUnknown)
            queried.use { assertEquals(instance.address, it.address) }
        }
    }

    @Test
    fun unsupportedQueriesFailWithoutAnInterfacePointer(): Unit = memScoped {
        val instance: IUnknown = ShellLink.new()
        instance.use {
            val iid = alloc<IID>()
            IDispatch.getIID(iid.ptr, IDispatch())
            val result = alloc<COpaquePointerVar> { value = instance.address }
            assertEquals(E_NOINTERFACE, instance.queryInterface(iid.ptr, result.ptr))
            assertNull(result.value)
            assertFailsWith<IllegalArgumentException> { instance.asCom<IDispatch, _>(IDispatch) }
        }
    }
}