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

import dev.karmakrafts.cominterop.vtable.VTableStruct
import dev.karmakrafts.cominterop.vtable.getVSelf
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.windows.HRESULT
import platform.windows.PCWSTR

/**
 * VTable-backed delegate used by `RoGetParameterizedTypeInstanceIID` to resolve WinRT metadata.
 *
 * @param function Callback receiving a UTF-16 type name pointer and the destination metadata builder pointer.
 */
@ExperimentalForeignApi
class IRoMetaDataLocator(
    private val function: (nameElement: PCWSTR, metaDataDestination: COpaquePointer) -> HRESULT
) : VTableStruct(1) {
    init {
        vTable["Locate"] = staticCFunction<COpaquePointer, PCWSTR, COpaquePointer, HRESULT> { self, name, dest ->
            self.getVSelf<IRoMetaDataLocator>().function(name, dest)
        }
    }
}