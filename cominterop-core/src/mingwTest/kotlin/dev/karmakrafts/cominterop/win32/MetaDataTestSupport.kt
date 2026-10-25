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

@file:OptIn(ExperimentalForeignApi::class)

package dev.karmakrafts.cominterop.win32

import dev.karmakrafts.cominterop.ComRuntime
import dev.karmakrafts.cominterop.HSTRINGVar
import dev.karmakrafts.cominterop.asCom
import dev.karmakrafts.cominterop.dll.WinTypes
import dev.karmakrafts.cominterop.hstring
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.S_OK
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal fun withMetaDataImport(name: String, block: (IMetaDataImport2, MdTypeDef) -> Unit) = memScoped {
    val filePath = alloc<HSTRINGVar> { value = null }
    val address = alloc<COpaquePointerVar> { value = null }
    val token = alloc<MdTypeDefVar>()
    try {
        assertEquals(S_OK, WinTypes.RoGetMetaDataFile(hstring(name), null, filePath.ptr, address.ptr, token.ptr))
        val import: IMetaDataImport2 = assertNotNull(address.value).asCom(IMetaDataImport2)
        import.use { block(it, token.value) }
    }
    finally {
        filePath.value?.let(ComRuntime::deleteString)
    }
}