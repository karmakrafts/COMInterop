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

package dev.karmakrafts.cominterop.win32

import dev.karmakrafts.cominterop.ComInterface
import dev.karmakrafts.cominterop.ComInterfaceType
import dev.karmakrafts.cominterop.ComRuntime
import dev.karmakrafts.cominterop.vtable.VTableFunctionList
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.cinterop.wcstr
import platform.posix.IID
import platform.posix.memcpy
import platform.windows.BYTEVar
import platform.windows.DWORD
import platform.windows.DWORDVar
import platform.windows.HRESULT
import platform.windows.LPCWSTR
import platform.windows.LPWSTR
import platform.windows.S_OK
import platform.windows.UINT32Var
import platform.windows.ULONG
import platform.windows.ULONGVar
import platform.windows.WCHARVar

@ExperimentalForeignApi
class IMetaDataImport : ComInterface<IMetaDataImport.Companion>(Companion) {
    private typealias _CloseEnum = (self: COpaquePointer, hEnum: HCORENUM) -> Unit
    private typealias _FindTypeDefByName = (self: COpaquePointer, szTypeDef: LPCWSTR, tkEnclosingClass: MdToken, ptkTypeDef: CPointer<MdTypeDefVar>) -> HRESULT
    private typealias _GetTypeDefProps = (self: COpaquePointer, tkTypeDef: MdTypeDef, szTypeDef: LPWSTR, cchTypeDef: ULONG, pchTypeDef: CPointer<ULONGVar>, pdwTypeDefFlags: CPointer<DWORDVar>, ptkExtends: CPointer<MdTokenVar>) -> HRESULT
    private typealias _GetCustomAttributeByName = (self: COpaquePointer, tkObj: MdToken, szName: LPCWSTR, ppData: CPointer<CPointerVar<BYTEVar>>, pcbData: CPointer<ULONGVar>) -> HRESULT

    companion object : ComInterfaceType {
        override val functions: List<String> = VTableFunctionList.buildFor<IMetaDataImport> {
            add("CloseEnum")
            add("FindTypeDefByName", precedingStubs = 5)
            add("GetTypeDefProps", precedingStubs = 2)
            add("GetCustomAttributeByName", precedingStubs = 47)
            addStubs(4)
        }

        override fun create(): ComInterface<*> = IMetaDataImport()

        override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) {
            ComRuntime.iidFromString("{7DAC8207-D3AE-4C75-9B67-92801A497D44}", iid)
        }
    }

    data class Props(
        val flags: DWORD, val typeName: String, val extends: MdToken
    )

    private val CloseEnum: CPointer<CFunction<_CloseEnum>> by vTable
    private val FindTypeDefByName: CPointer<CFunction<_FindTypeDefByName>> by vTable
    private val GetTypeDefProps: CPointer<CFunction<_GetTypeDefProps>> by vTable
    private val GetCustomAttributeByName: CPointer<CFunction<_GetCustomAttributeByName>> by vTable

    fun closeEnum(hEnum: HCORENUM) = CloseEnum(address, hEnum)

    fun findTypeDefByName(typeName: String, enclosingClass: MdToken): MdTypeDef? = memScoped {
        val typeDef = alloc<MdTypeDefVar>()
        if (FindTypeDefByName(address, typeName.wcstr.ptr, enclosingClass, typeDef.ptr) != S_OK) {
            return@memScoped null
        }
        typeDef.value
    }

    fun getTypeDefProps(typeDef: MdTypeDef): Props? = memScoped {
        val nameBuffer = allocArray<WCHARVar>(512)
        val nameLength = alloc<UINT32Var>()
        val flags = alloc<DWORDVar>()
        val extends = alloc<MdTokenVar>()
        if (GetTypeDefProps(
                address, typeDef, nameBuffer, 512U, nameLength.ptr, flags.ptr, extends.ptr
            ) != S_OK
        ) return@memScoped null
        Props(flags.value, nameBuffer.toKStringFromUtf16(), extends.value)
    }

    fun getCustomAttributeByName(typeDef: MdTypeDef, name: String): UByteArray? = memScoped {
        val blob = alloc<CPointerVar<UByteVar>>()
        val blobSize = alloc<ULONGVar>()
        if (GetCustomAttributeByName(
                address, typeDef, name.wcstr.ptr, blob.ptr, blobSize.ptr
            ) != S_OK
        ) return@memScoped null
        UByteArray(blobSize.value.toInt()).apply {
            usePinned { pinnedArray ->
                memcpy(pinnedArray.addressOf(0), blob.value, blobSize.value.convert())
            }
        }
    }
}
