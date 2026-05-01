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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.IID
import platform.windows.HRESULT
import platform.windows.S_OK
import platform.windows.ULONG
import platform.windows.ULONGVar

@ExperimentalForeignApi
class IMetaDataImport2 : ComInterface<IMetaDataImport2.Companion>(Companion) {
    private typealias _EnumGenericParams = (self: COpaquePointer, phEnum: CPointer<HCORENUMVar>, tk: MdToken, rGenericParams: CPointer<MdGenericParamVar>, cMax: ULONG, pcGenericParams: CPointer<ULONGVar>) -> HRESULT

    companion object : ComInterfaceType {
        override val superInterfaces: Array<ComInterfaceType> = arrayOf(IMetaDataImport)

        override val functions: List<String> = VTableFunctionList.buildFor<IMetaDataImport2> {
            add("EnumGenericParams")
            addStubs(7)
        }

        override fun create(): ComInterface<*> = IMetaDataImport2()

        override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) {
            ComRuntime.iidFromString("{FCE5EFA0-8BBA-4f8E-A036-8F2022B08466}", iid)
        }
    }

    private val EnumGenericParams: CPointer<CFunction<_EnumGenericParams>> by vTable

    fun enumGenericParams(hEnum: CPointer<HCORENUMVar>, token: MdToken): List<MdGenericParam> = memScoped {
        val params = ArrayList<MdGenericParam>()
        // Use a sliding window iterator approach to gather all parameters
        val genericParams = allocArray<MdGenericParamVar>(16)
        val paramCount = alloc<ULONGVar>()
        while (EnumGenericParams(address, hEnum, token, genericParams, 16U, paramCount.ptr) == S_OK) {
            if (paramCount.value == 0U) break
            params += (0..<paramCount.value.toInt()).map(genericParams::get)
        }
        params
    }
}