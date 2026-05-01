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
import dev.karmakrafts.cominterop.win32.MdTypeDefVar
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.atexit
import platform.windows.DWORDVar
import platform.windows.HRESULT

@ExperimentalForeignApi
object WinTypes {
    private typealias _RoParseTypeName = (typeName: HSTRING, partsCount: CPointer<DWORDVar>, typeNameParts: CPointer<CPointerVar<HSTRINGVar>>) -> HRESULT
    private typealias _RoGetMetaDataFile = (name: HSTRING, metaDataDispenser: COpaquePointer?, metaDataFilePath: CPointer<HSTRINGVar>?, metaDataImport: CPointer<COpaquePointerVar>?, typeDefToken: CPointer<MdTypeDefVar>?) -> HRESULT

    private val library: SharedLibrary = requireNotNull(SharedLibrary.open("WinTypes.dll")) {
        "Could not load WinTypes.dll"
    }

    init {
        atexit(staticCFunction<Unit> {
            val self = WinTypes
            self.library.close()
        })
    }

    val RoParseTypeName: CPointer<CFunction<_RoParseTypeName>> by library
    val RoGetMetaDataFile: CPointer<CFunction<_RoGetMetaDataFile>> by library
}