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

package dev.karmakrafts.cominterop

import dev.karmakrafts.cominterop.vtable.VTableStruct
import dev.karmakrafts.cominterop.vtable.getSelf
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import platform.posix.IID
import platform.windows.E_NOINTERFACE
import platform.windows.HRESULT
import platform.windows.IID_IUnknown
import platform.windows.S_OK
import platform.windows.ULONG
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch

/**
 * [IUnknown on MSDN](https://learn.microsoft.com/en-us/windows/win32/api/unknwn/nn-unknwn-iunknown)
 */
@ExperimentalForeignApi
class IUnknown : ComInterface<IUnknown.Companion>(Companion) {
    companion object : ComInterfaceType {
        override val superInterfaces: Array<ComInterfaceType> = emptyArray()
        override val functions: List<String> = listOf("QueryInterface", "AddRef", "Release")
        override fun create(): ComInterface<*> = IUnknown()
        override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) = ComRuntime.copy(iid, IID_IUnknown.ptr)
    }
}

@ExperimentalForeignApi
abstract class IUnknownImpl(
    functionCount: Int
) : VTableStruct(functionCount + 3) {
    protected var refCount: AtomicInt = AtomicInt(1)

    init { // COM objects use static trampolines since we can't directly turn a closure into a C function
        vTable["QueryInterface"] = staticCFunction<COpaquePointer, CPointer<IID>, CPointer<COpaquePointerVar>, HRESULT> { self, iid, ppvObject ->
            self.getSelf<IUnknownImpl>().queryInterface(self, iid, ppvObject)
        }
        vTable["AddRef"] = staticCFunction<COpaquePointer, ULONG> { self ->
            self.getSelf<IUnknownImpl>().addRef(self)
        }
        vTable["Release"] = staticCFunction<COpaquePointer, ULONG> { self ->
            self.getSelf<IUnknownImpl>().release(self)
        }
    }

    open fun queryInterface(self: COpaquePointer, iid: CPointer<IID>, ppvObject: CPointer<COpaquePointerVar>): HRESULT {
        if (ComRuntime.iidEquals(iid, IID_IUnknown.ptr)) {
            ppvObject.pointed.value = self
            return S_OK
        }
        ppvObject.pointed.value = null
        return E_NOINTERFACE
    }

    open fun addRef(self: COpaquePointer): ULONG {
        check(refCount.load() > 0) { "Cannot acquire dead reference to COM implementation" }
        return refCount.incrementAndFetch().toUInt()
    }

    @Suppress("DEPRECATION")
    open fun release(self: COpaquePointer): ULONG {
        check(refCount.load() > 0) { "Cannot release dead reference to COM implementation" }
        val newCount = refCount.decrementAndFetch().toUInt()
        if (newCount == 0U) close()
        return newCount
    }

    @Deprecated( // @formatter:off
        message = "Don't use close directly on COM implementations",
        replaceWith = ReplaceWith("release"),
        level = DeprecationLevel.WARNING
    ) // @formatter:on
    override fun close() {
        super.close()
    }
}

