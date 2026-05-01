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

package dev.karmakrafts.cominterop.dll

import dev.karmakrafts.cominterop.HSTRING
import dev.karmakrafts.cominterop.HSTRINGVar
import dev.karmakrafts.cominterop.HSTRING_HEADER
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.CLSID
import platform.posix.GUID
import platform.posix.IID
import platform.posix.LPCLSID
import platform.posix.LPIID
import platform.posix.atexit
import platform.windows.DWORD
import platform.windows.HRESULT
import platform.windows.LPCOLESTR
import platform.windows.LPOLESTRVar
import platform.windows.LPUNKNOWN
import platform.windows.LPVOID
import platform.windows.LPVOIDVar
import platform.windows.PCWSTR
import platform.windows.PCWSTRVar
import platform.windows.SIZE_T
import platform.windows.UINT32
import platform.windows.UINT32Var

/**
 * Provides all common functions used for interacting with COM objects on Windows.
 * This includes most Windows_, Co_ and Ro_ prefixed functions.
 */
@ExperimentalForeignApi
internal object ComBase {
    private typealias _RoInitialize = (initType: Int) -> HRESULT
    private typealias _RoUninitialize = () -> Unit
    private typealias _RoActivateInstance = (classId: HSTRING, instance: CPointer<COpaquePointerVar>) -> HRESULT
    private typealias _RoGetActivationFactory = (classId: HSTRING, iid: CPointer<IID>, factory: CPointer<COpaquePointerVar>) -> HRESULT
    private typealias _RoGetParameterizedTypeInstanceIID = (nameElementCount: UINT32, nameElements: CPointer<PCWSTRVar>, metaDataLocator: COpaquePointer, iid: CPointer<GUID>, pExtra: COpaquePointer?) -> HRESULT

    private typealias _CoInitializeEx = (pvReserved: LPVOID?, dwCoInit: DWORD) -> HRESULT
    private typealias _CoUninitialize = () -> Unit
    private typealias _CoCreateInstance = (rclsid: CPointer<CLSID>?, pUnkOuter: LPUNKNOWN?, dwClsContext: DWORD, riid: CPointer<IID>?, ppv: CPointer<LPVOIDVar>) -> HRESULT
    private typealias _CoTaskMemFree = (pv: LPVOID) -> Unit
    private typealias _CoTaskMemAlloc = (cb: SIZE_T) -> LPVOID
    private typealias _CLSIDFromProgID = (lpszProgId: LPCOLESTR, lpclsid: LPCLSID) -> HRESULT
    private typealias _IIDFromString = (lpsz: LPCOLESTR, lpiid: LPIID) -> HRESULT
    private typealias _StringFromCLSID = (rclsid: CPointer<CLSID>, lplpsz: CPointer<LPOLESTRVar>) -> HRESULT

    private typealias _WindowsCreateString = (src: PCWSTR, length: UINT32, out: CPointer<HSTRINGVar>) -> HRESULT
    private typealias _WindowsCreateStringReference = (src: PCWSTR, length: UINT32, header: CPointer<HSTRING_HEADER>, out: CPointer<HSTRINGVar>) -> HRESULT
    private typealias _WindowsDeleteString = (str: HSTRING) -> HRESULT
    private typealias _WindowsGetStringRawBuffer = (str: HSTRING, length: CPointer<UINT32Var>?) -> PCWSTR

    const val RO_INIT_MULTITHREADED = 1

    private val library: SharedLibrary = requireNotNull(SharedLibrary.open("combase.dll")) {
        "Could not load combase.dll"
    }

    init {
        atexit(staticCFunction<Unit> {
            val self = ComBase // To avoid formatter removing explicit ref
            self.library.close()
        })
    }

    val RoInitialize: CPointer<CFunction<_RoInitialize>> by library
    val RoUninitialize: CPointer<CFunction<_RoUninitialize>> by library
    val RoActivateInstance: CPointer<CFunction<_RoActivateInstance>> by library
    val RoGetActivationFactory: CPointer<CFunction<_RoGetActivationFactory>> by library
    val RoGetParameterizedTypeInstanceIID: CPointer<CFunction<_RoGetParameterizedTypeInstanceIID>> by library

    val CoInitializeEx: CPointer<CFunction<_CoInitializeEx>> by library
    val CoUninitialize: CPointer<CFunction<_CoUninitialize>> by library
    val CoCreateInstance: CPointer<CFunction<_CoCreateInstance>> by library
    val CoTaskMemAlloc: CPointer<CFunction<_CoTaskMemAlloc>> by library
    val CoTaskMemFree: CPointer<CFunction<_CoTaskMemFree>> by library
    val CLSIDFromProgID: CPointer<CFunction<_CLSIDFromProgID>> by library
    val IIDFromString: CPointer<CFunction<_IIDFromString>> by library
    val StringFromCLSID: CPointer<CFunction<_StringFromCLSID>> by library

    val WindowsCreateString: CPointer<CFunction<_WindowsCreateString>> by library
    val WindowsCreateStringReference: CPointer<CFunction<_WindowsCreateStringReference>> by library
    val WindowsDeleteString: CPointer<CFunction<_WindowsDeleteString>> by library
    val WindowsGetStringRawBuffer: CPointer<CFunction<_WindowsGetStringRawBuffer>> by library
}
