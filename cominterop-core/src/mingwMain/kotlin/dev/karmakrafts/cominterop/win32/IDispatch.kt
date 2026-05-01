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
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import platform.posix.IID
import platform.windows.DISPID
import platform.windows.DISPIDVar
import platform.windows.DISPPARAMS
import platform.windows.EXCEPINFO
import platform.windows.HRESULT
import platform.windows.LCID
import platform.windows.LPOLESTRVar
import platform.windows.UINT
import platform.windows.UINTVar
import platform.windows.VARIANT
import platform.windows.WORD

/**
 * [IDispatch on MSDN](https://learn.microsoft.com/en-us/windows/win32/api/oaidl/nn-oaidl-idispatch)
 */
@ExperimentalForeignApi
class IDispatch : ComInterface<IDispatch.Companion>(Companion) {
    private typealias _GetIDsOfNames = (self: COpaquePointer, riid: IID, rgszNames: CPointer<LPOLESTRVar>, cNames: UINT, lcid: LCID, rgDispId: CPointer<DISPIDVar>) -> HRESULT
    private typealias _GetTypeInfo = (self: COpaquePointer, iTInfo: UINT, lcid: LCID, ppTInfo: CPointer<COpaquePointerVar>) -> HRESULT
    private typealias _GetTypeInfoCount = (self: COpaquePointer, pctinfo: CPointer<UINTVar>) -> HRESULT
    private typealias _Invoke = (self: COpaquePointer, dispIdMember: DISPID, riid: IID, lcid: LCID, wFlags: WORD, pDispParams: CPointer<DISPPARAMS>, pVarResult: CPointer<VARIANT>, pExcepInfo: CPointer<EXCEPINFO>, puArgErr: CPointer<UINTVar>) -> HRESULT

    companion object : ComInterfaceType {
        override val functions: List<String> = VTableFunctionList.build {
            add("GetIDsOfNames")
            add("GetTypeInfo")
            add("GetTypeInfoCount")
            add("Invoke")
        }

        override fun create(): ComInterface<*> = IDispatch()

        override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) {
            ComRuntime.iidFromString("{00020400-0000-0000-C000-000000000046}", iid)
        }
    }

    private val GetIDsOfNames: CPointer<CFunction<_GetIDsOfNames>> by vTable
    private val GetTypeInfo: CPointer<CFunction<_GetTypeInfo>> by vTable
    private val GetTypeInfoCount: CPointer<CFunction<_GetTypeInfoCount>> by vTable
    private val Invoke: CPointer<CFunction<_Invoke>> by vTable

    fun getIDsOfNames(
        riid: IID, rgszNames: CPointer<LPOLESTRVar>, cNames: UINT, lcid: LCID, rgDispId: CPointer<DISPIDVar>
    ): HRESULT = GetIDsOfNames(address, riid, rgszNames, cNames, lcid, rgDispId)

    fun getTypeInfo(iTInfo: UINT, lcid: LCID, ppTInfo: CPointer<COpaquePointerVar>): HRESULT =
        GetTypeInfo(address, iTInfo, lcid, ppTInfo)

    fun getTypeInfoCount(pctinfo: CPointer<UINTVar>): HRESULT = GetTypeInfoCount(address, pctinfo)

    operator fun invoke(
        dispIdMember: DISPID,
        riid: IID,
        lcid: LCID,
        wFlags: WORD,
        pDispParams: CPointer<DISPPARAMS>,
        pVarResult: CPointer<VARIANT>,
        pExcepInfo: CPointer<EXCEPINFO>,
        puArgErr: CPointer<UINTVar>
    ): HRESULT = Invoke(address, dispIdMember, riid, lcid, wFlags, pDispParams, pVarResult, pExcepInfo, puArgErr)
}