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

import dev.karmakrafts.cominterop.dll.ComBase
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.value
import kotlinx.cinterop.wcstr
import platform.posix.CLSID
import platform.posix.GUID
import platform.posix.IID
import platform.posix.memcmp
import platform.posix.memcpy
import platform.windows.COINIT_MULTITHREADED
import platform.windows.LPOLESTRVar
import platform.windows.S_OK

@ExperimentalForeignApi
object ComRuntime {
    fun init() {
        ComBase.CoInitializeEx(null, COINIT_MULTITHREADED)
        ComBase.RoInitialize(ComBase.RO_INIT_MULTITHREADED)
    }

    fun uninit() {
        ComBase.CoUninitialize()
        ComBase.RoUninitialize()
    }

    fun createString(value: String): HSTRING = memScoped {
        val handle = alloc<HSTRINGVar>()
        check(ComBase.WindowsCreateString(value.wcstr.ptr, value.length.toUInt(), handle.ptr) == S_OK) {
            "Could not allocate HSTRING '$value'"
        }
        requireNotNull(handle.value) { "Could not allocate HSTRING '$value'" }
    }

    fun deleteString(handle: HSTRING) = ComBase.WindowsDeleteString(handle)

    fun getString(handle: HSTRING): String = ComBase.WindowsGetStringRawBuffer(handle, null).toKStringFromUtf16()

    fun iidFromString(value: String, iid: CPointer<IID>) {
        require(value.length == 38) { "Invalid length for IID '$value'" }
        memScoped {
            check(ComBase.IIDFromString(value.wcstr.ptr, iid) == S_OK) {
                "Could not create IID from string '$value'"
            }
        }
    }

    fun iidEquals(iid1: CPointer<IID>, iid2: CPointer<IID>): Boolean {
        if (iid1 == iid2) return true // If addresses are equal, take short path
        return memcmp(iid1, iid2, sizeOf<IID>().convert()) == 0
    }

    fun iidEquals(iid1: CPointer<IID>, iid2: String): Boolean = memScoped {
        val parsedIid2 = alloc<IID>()
        iidFromString(iid2, parsedIid2.ptr)
        iidEquals(iid1, parsedIid2.ptr)
    }

    fun clsidFromProgId(progId: String, clsid: CPointer<CLSID>) {
        memScoped {
            check(ComBase.CLSIDFromProgID(progId.wcstr.ptr, clsid) == S_OK) {
                "Could not find CLSID for PROGID '$progId'"
            }
        }
    }

    fun guidToString(guid: GUID): String = memScoped {
        val ptr = alloc<LPOLESTRVar>()
        ComBase.StringFromCLSID(guid.ptr, ptr.ptr)
        val result = requireNotNull(ptr.value).toKStringFromUtf16()
        ComBase.CoTaskMemFree(ptr.value!!)
        result
    }

    inline fun <reified T : CVariable> copy(dst: CPointer<T>, src: CPointer<T>) {
        memcpy(dst, src, sizeOf<T>().convert())
    }
}

@ExperimentalForeignApi
fun MemScope.hstring(value: String): HSTRING {
    val handle = alloc<HSTRINGVar>()
    val header = alloc<HSTRING_HEADER>()
    check(
        ComBase.WindowsCreateStringReference(
            value.wcstr.ptr, value.length.toUInt(), header.ptr, handle.ptr
        ) == S_OK
    ) {
        "Could not allocate temporary HSTRING in memory scope"
    }
    return requireNotNull(handle.value) { "Could not allocate temporary HSTRING" }
}
