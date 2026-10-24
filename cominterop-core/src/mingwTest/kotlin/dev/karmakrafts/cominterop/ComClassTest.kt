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
import dev.karmakrafts.cominterop.winrt.RtClass
import dev.karmakrafts.cominterop.winrt.activate
import dev.karmakrafts.cominterop.winrt.collections.IPropertySet
import dev.karmakrafts.cominterop.winrt.collections.PropertySet
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.CLSID
import platform.posix.IID
import platform.windows.CLSCTX_INPROC_SERVER
import platform.windows.E_NOINTERFACE
import platform.windows.S_OK
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class ComClassTest {
    private object ShellLink : ComClass<IUnknown.Companion> {
        override val defaultInterface: IUnknown.Companion = IUnknown

        override fun getCLSID(clsid: CPointer<CLSID>) {
            ComRuntime.iidFromString("{00021401-0000-0000-C000-000000000046}", clsid)
        }
    }

    private object UnregisteredClass : ComClass<IUnknown.Companion> {
        override val defaultInterface: IUnknown.Companion = IUnknown

        override fun getCLSID(clsid: CPointer<CLSID>) {
            ComRuntime.iidFromString("{00000000-0000-0000-0000-000000000000}", clsid)
        }
    }

    private object UnregisteredRtClass : RtClass {
        override val rtTypeName: String = "COMInterop.Tests.UnregisteredClass"
    }

    @BeforeTest
    fun initRuntime() {
        ComRuntime.init()
    }

    @AfterTest
    fun uninitRuntime() {
        ComRuntime.uninit()
    }

    @Test
    fun newCreatesTheDefaultInterfaceWithDefaultAndExplicitContext() {
        val default: IUnknown = ShellLink.new()
        default.use { assertSame(IUnknown, it.interfaceType) }
        val explicit: IUnknown = ShellLink.new(CLSCTX_INPROC_SERVER.toUInt())
        explicit.use { assertSame(IUnknown, it.interfaceType) }
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
        queried.use {
            assertEquals(instance.address, it.address)
            val another: IUnknown = it.asCom(IUnknown)
            another.use { assertEquals(queried.address, it.address) }
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

    @Test
    fun unregisteredComClassCannotBeCreated() {
        assertFailsWith<IllegalStateException> { UnregisteredClass.new<IUnknown, _, _>() }
    }

    @Test
    fun winrtActivationSupportsQueryingBaseInterfaces() {
        val instance: IPropertySet = PropertySet.activate(IPropertySet)
        instance.use {
            assertSame(IPropertySet, it.interfaceType)
            it.asCom(IInspectable).use { inspectable ->
                inspectable.asCom(IUnknown).use { unknown ->
                    assertSame(IUnknown, unknown.interfaceType)
                }
            }
        }
    }

    @Test
    fun unregisteredWinrtClassCannotBeActivated() {
        assertFailsWith<IllegalArgumentException> {
            UnregisteredRtClass.activate<IPropertySet, _, _>(IPropertySet)
        }
    }
}