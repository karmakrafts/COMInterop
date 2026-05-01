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

import dev.karmakrafts.cominterop.vtable.VTable
import dev.karmakrafts.cominterop.vtable.VTableFunctionList
import dev.karmakrafts.cominterop.vtable.VTableInterface
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.wcstr
import platform.posix.GUID
import platform.windows.HRESULT
import platform.windows.PCWSTR
import platform.windows.PCWSTRVar
import platform.windows.UINT32

@ExperimentalForeignApi
class IRoSimpleMetaDataBuilder(address: COpaquePointer) : VTableInterface(10) {
    private typealias _SetWinRtInterface = (self: COpaquePointer, iid: GUID) -> HRESULT
    private typealias _SetStruct = (self: COpaquePointer, name: PCWSTR, numFields: UINT32, fieldTypeNames: CPointer<PCWSTRVar>?) -> HRESULT
    private typealias _SetParametrizedInterface = (self: COpaquePointer, iid: GUID, numArgs: UINT32) -> HRESULT

    init {
        init(address)
    }

    override fun populateVTable(vTable: VTable) {
        vTable += VTableFunctionList.build {
            add("SetWinRtInterface")
            add("SetStruct", precedingStubs = 5)
            add("SetParametrizedInterface", precedingStubs = 1)
            addStubs(1)
        }
    }

    private val SetWinRtInterface: CPointer<CFunction<_SetWinRtInterface>> by vTable
    private val SetStruct: CPointer<CFunction<_SetStruct>> by vTable
    private val SetParametrizedInterface: CPointer<CFunction<_SetParametrizedInterface>> by vTable

    fun setWinRtInterface(iid: GUID): HRESULT {
        return SetWinRtInterface(address, iid)
    }

    fun setPrimitive(name: String): HRESULT = memScoped {
        SetStruct(address, name.wcstr.ptr, 0U, null)
    }

    fun setParametrizedInterface(iid: GUID, numArgs: UINT32): HRESULT {
        return SetParametrizedInterface(address, iid, numArgs)
    }
}
