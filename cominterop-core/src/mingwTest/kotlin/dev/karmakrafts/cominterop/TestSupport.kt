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

import dev.karmakrafts.cominterop.winrt.IInspectable
import dev.karmakrafts.cominterop.winrt.RtInterfaceType
import dev.karmakrafts.cominterop.winrt.RtType
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.CLSID
import platform.posix.IID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

abstract class ComTest {
    @BeforeTest
    fun initRuntime() {
        ComRuntime.init()
    }

    @AfterTest
    fun uninitRuntime() {
        ComRuntime.uninit()
    }
}

internal object ShellLink : ComClass<IUnknown.Companion> {
    override val defaultInterface: IUnknown.Companion = IUnknown

    override fun getCLSID(clsid: CPointer<CLSID>) {
        ComRuntime.iidFromString("{00021401-0000-0000-C000-000000000046}", clsid)
    }
}

internal class TestUnknown : IUnknownImpl(0) {
    var isClosed: Boolean = false
        private set

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun close() {
        isClosed = true
        super.close()
    }
}

internal fun assertComDescriptor(type: ComInterfaceType, iid: String, functionCount: Int) = memScoped {
    val first = type.create()
    val second = type.create()
    assertNotSame(first, second)
    assertSame(type, first.interfaceType)
    assertSame(type, second.interfaceType)
    assertEquals(functionCount, type.getTotalFunctionCount())
    val actual = alloc<IID>()
    type.getIID(actual.ptr, first)
    assertEquals(iid, ComRuntime.guidToString(actual))
}

internal fun assertRtDescriptor(
    type: RtInterfaceType, name: String, args: List<RtType>, functionCount: Int
) {
    assertEquals("Windows.Foundation.Collections.$name", type.rtTypeName)
    assertEquals(args.size, type.arity)
    assertEquals(listOf(IUnknown, IInspectable), type.allSuperInterfaces())
    assertEquals(functionCount, type.getTotalFunctionCount())
    val first = type.create(args)
    val second = type.create(args)
    assertNotSame(first, second)
    assertSame(type, first.interfaceType)
    assertSame(type, second.interfaceType)
    assertEquals(args, first.instantiatedType.args)
    assertEquals(args, second.instantiatedType.args)
}