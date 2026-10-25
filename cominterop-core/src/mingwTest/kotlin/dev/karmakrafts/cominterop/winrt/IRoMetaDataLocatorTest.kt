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

package dev.karmakrafts.cominterop.winrt

import dev.karmakrafts.cominterop.vtable.VTable
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.wcstr
import platform.windows.E_INVALIDARG
import platform.windows.HRESULT
import platform.windows.PCWSTR
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IRoMetaDataLocatorTest {
    @Test
    fun nativeTrampolineForwardsUtf16NameDestinationAndResult() = memScoped {
        val destination = alloc<IntVar>().ptr
        var receivedName: String? = null
        var receivedDestination: COpaquePointer? = null
        IRoMetaDataLocator { name, builder ->
            receivedName = name.toKStringFromUtf16()
            receivedDestination = builder
            E_INVALIDARG
        }.use { locator ->
            val table = VTable(assertNotNull(locator.data.vTable), 1)
            table += "Locate"
            val locate = table.get<(COpaquePointer, PCWSTR, COpaquePointer) -> HRESULT>("Locate")
            assertEquals(E_INVALIDARG, locate(locator.address, "Metadata.日本語".wcstr.ptr, destination))
            assertEquals("Metadata.日本語", receivedName)
            assertEquals(destination, receivedDestination)
        }
    }
}