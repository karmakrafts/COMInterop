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

package dev.karmakrafts.cominterop.win32

import dev.karmakrafts.cominterop.ComClass
import dev.karmakrafts.cominterop.ComRuntime
import dev.karmakrafts.cominterop.ComTest
import dev.karmakrafts.cominterop.IUnknown
import dev.karmakrafts.cominterop.assertComDescriptor
import dev.karmakrafts.cominterop.new
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.CLSID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class IDispatchTest : ComTest() {
    private object ShellApplication : ComClass<IDispatch.Companion> {
        override val defaultInterface: IDispatch.Companion = IDispatch

        override fun getCLSID(clsid: CPointer<CLSID>) {
            ComRuntime.clsidFromProgId("Shell.Application", clsid)
        }
    }

    @Test
    fun dispatchInheritsUnknownByDefault() {
        assertEquals(listOf(IUnknown), IDispatch.superInterfaces.toList())
        assertEquals(listOf(IUnknown), IDispatch.allSuperInterfaces())
        assertIs<IDispatch>(IDispatch.create())
        assertComDescriptor(IDispatch, "{00020400-0000-0000-C000-000000000046}", 7)
    }

    @Test
    fun shellAutomationSupportsQueryingDispatch() {
        val instance: IDispatch = ShellApplication.new()
        instance.use { dispatch ->
            dispatch.asCom(IUnknown).asCom(IDispatch).use {
                assertSame(IDispatch, it.interfaceType)
            }
        }
    }
}