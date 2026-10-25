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
import platform.posix.CLSID
import platform.windows.CLSCTX_INPROC_SERVER
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ComClassTest : ComTest() {
    private object UnregisteredClass : ComClass<IUnknown.Companion> {
        override val defaultInterface: IUnknown.Companion = IUnknown

        override fun getCLSID(clsid: CPointer<CLSID>) {
            ComRuntime.iidFromString("{00000000-0000-0000-0000-000000000000}", clsid)
        }
    }

    @Test
    fun newCreatesTheDefaultInterfaceWithDefaultAndExplicitContext() {
        val default: IUnknown = ShellLink.new()
        default.use { assertSame(IUnknown, it.interfaceType) }
        val explicit: IUnknown = ShellLink.new(CLSCTX_INPROC_SERVER.toUInt())
        explicit.use { assertSame(IUnknown, it.interfaceType) }
    }

    @Test
    fun unregisteredComClassCannotBeCreated() {
        assertFailsWith<IllegalStateException> { UnregisteredClass.new<IUnknown, _, _>() }
    }
}