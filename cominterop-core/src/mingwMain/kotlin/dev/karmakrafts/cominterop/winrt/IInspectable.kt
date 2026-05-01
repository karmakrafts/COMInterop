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

package dev.karmakrafts.cominterop.winrt

import dev.karmakrafts.cominterop.ComInterface
import dev.karmakrafts.cominterop.ComInterfaceType
import dev.karmakrafts.cominterop.ComRuntime
import dev.karmakrafts.cominterop.HSTRINGVar
import dev.karmakrafts.cominterop.IUnknown
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.get
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.IID
import platform.windows.HRESULT
import platform.windows.S_OK
import platform.windows.ULONGVar

/**
 * [IInspectable on MSDN](https://learn.microsoft.com/en-us/windows/win32/api/inspectable/nn-inspectable-iinspectable)
 */
@ExperimentalForeignApi
class IInspectable : RtInterface<IInspectable.Companion>(Companion, emptyList()) {
    private typealias _GetIids = (self: COpaquePointer, iidCount: CPointer<ULONGVar>, iids: CPointer<CPointerVar<IID>>) -> HRESULT
    private typealias _GetRuntimeClassName = (self: COpaquePointer, className: CPointer<HSTRINGVar>) -> HRESULT
    private typealias _GetTrustLevel = (self: COpaquePointer, level: Int) -> HRESULT

    companion object : RtInterfaceType {
        override val superInterfaces: Array<ComInterfaceType> = arrayOf(IUnknown)
        override val functions: List<String> = listOf("GetIids", "GetRuntimeClassName", "GetTrustLevel")
        override val rtTypeName: String = "Object" // IInspectable is projected as Object in the ABI
        override val isInstantiated: Boolean = true // IInspectable is parameterless and always considered instantiated

        override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) {
            ComRuntime.iidFromString("{AF86E2E0-B12D-4C6A-9C5A-D7AA65101E90}", iid)
        }

        override fun create(typeArgs: List<RtType>): RtInterface<*> = IInspectable()
    }

    private val GetIids: CPointer<CFunction<_GetIids>> by vTable
    private val GetRuntimeClassName: CPointer<CFunction<_GetRuntimeClassName>> by vTable
    private val GetTrustLevel: CPointer<CFunction<_GetTrustLevel>> by vTable

    /**
     * Runtime class name reported by the underlying object, if available.
     */
    val runtimeClassName: String?
        get() = memScoped {
            val name = alloc<HSTRINGVar>()
            if (GetRuntimeClassName(address, name.ptr) != S_OK) return@memScoped null
            ComRuntime.getString(name.value ?: return@memScoped null)
        }

    /**
     * Retrieves all interface identifiers implemented by this object.
     *
     * @return The list of supported IIDs, or an empty list if retrieval fails.
     */
    fun getIIds(): List<IID> = memScoped {
        val iids = allocPointerTo<IID>()
        val count = alloc<ULONGVar>()
        if (GetIids(address, count.ptr, iids.ptr) != S_OK) return@memScoped emptyList()
        (0..<count.value.toInt()).map { idx -> iids.value!![idx] }.toList()
    }
}
