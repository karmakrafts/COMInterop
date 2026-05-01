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

package dev.karmakrafts.cominterop.winrt.collections

import dev.karmakrafts.cominterop.vtable.VTableFunctionList
import dev.karmakrafts.cominterop.winrt.RtInterface
import dev.karmakrafts.cominterop.winrt.RtInterfaceType
import dev.karmakrafts.cominterop.winrt.RtType
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.uint32_tVar
import platform.windows.HRESULT
import platform.windows.S_OK
import platform.windows.booleanVar

/**
 * [IMap on MSDN](https://learn.microsoft.com/en-us/uwp/api/windows.foundation.collections.imap-2?view=winrt-26100)
 *
 * @param typeArgs Generic type arguments in key/value order.
 */
@ExperimentalForeignApi
class IMap(typeArgs: List<RtType>) : RtInterface<IMap.Companion>(Companion, typeArgs) {
    private typealias _GetSize = (self: COpaquePointer, size: CPointer<uint32_tVar>) -> HRESULT
    private typealias _Lookup = (self: COpaquePointer, key: COpaquePointer, value: COpaquePointer) -> HRESULT
    private typealias _Insert = (self: COpaquePointer, key: COpaquePointer, value: COpaquePointer, replaced: CPointer<booleanVar>) -> HRESULT

    companion object : RtInterfaceType {
        override val rtTypeName: String = "Windows.Foundation.Collections.IMap"
        override val arity: Int = 2

        override val functions: List<String> = VTableFunctionList.buildFor<IMap> {
            add("Lookup")
            add("GetSize")
            add("Insert", precedingStubs = 2)
            addStubs(2)
        }

        override fun create(typeArgs: List<RtType>): RtInterface<*> = IMap(typeArgs)
    }

    private val GetSize: CPointer<CFunction<_GetSize>> by vTable
    private val Lookup: CPointer<CFunction<_Lookup>> by vTable
    private val Insert: CPointer<CFunction<_Insert>> by vTable

    /** Number of entries in the map, or `-1` if retrieval fails. */
    val size: Int
        get() = memScoped {
            val value = alloc<uint32_tVar>()
            if (GetSize(address, value.ptr) != S_OK) return@memScoped -1
            value.value.toInt()
        }

    /** Range of map indices based on [size]. */
    inline val indices: IntRange
        get() = 0..<size

    /**
     * Looks up a value by [key].
     *
     * @param key Native key pointer.
     * @param value Destination pointer receiving the value.
     * @return `true` when lookup succeeds.
     */
    fun lookup(key: COpaquePointer, value: COpaquePointer): Boolean {
        return Lookup(address, key, value) == S_OK
    }

    /**
     * Inserts or replaces a value at [key].
     *
     * @param key Native key pointer.
     * @param value Native value pointer.
     * @return `true` when insertion succeeds.
     */
    fun insert(key: COpaquePointer, value: COpaquePointer): Boolean = memScoped {
        val replaced = alloc<booleanVar>()
        return Insert(address, key, value, replaced.ptr) == S_OK
    }
}
