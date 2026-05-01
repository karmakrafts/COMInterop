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

package dev.karmakrafts.cominterop.vtable

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alignOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.free
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf

/**
 * Base class for COM implementations created from within Kotlin code.
 * Since COM objects are just `void***`, we allocate the v-table as a contiguous block of pointers,
 * and store the base address to that v-table in a pre-allocated sizeof(void*) wrapper structure.
 */
@ExperimentalForeignApi
abstract class VTableStruct(
    functionCount: Int
) : AutoCloseable {
    protected val selfRef: StableRef<VTableStruct> = StableRef.create(this)

    protected val vTableAddress: COpaquePointer = requireNotNull(
        interpretCPointer(
            nativeHeap.alloc(
                sizeOf<CPointerVar<*>>() * functionCount.toLong(), alignOf<CPointerVar<*>>()
            ).rawPtr
        )
    ) {
        "Could not allocate memory for COM implementation v-table"
    }

    val data: VTableData = nativeHeap.alloc {
        vTable = vTableAddress
        selfRef = this@VTableStruct.selfRef.asCPointer()
    }

    protected val vTable: VTable = VTable(vTableAddress, functionCount)
    inline val address: COpaquePointer get() = data.ptr

    override fun close() {
        selfRef.dispose()
        nativeHeap.free(vTableAddress)
        nativeHeap.free(data)
    }
}

// Cannot be an instance function since that would implicitly capture this-ref in staticCFunction
@ExperimentalForeignApi
internal inline fun <reified I : VTableStruct> COpaquePointer.getSelf(): I =
    requireNotNull(reinterpret<VTableData>().pointed.selfRef) {
        "Could not retrieve self reference address"
    }.asStableRef<I>().get()
