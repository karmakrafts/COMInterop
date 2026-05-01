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
import dev.karmakrafts.cominterop.winrt.asRt
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.uint32_t
import platform.posix.uint32_tVar
import platform.windows.HRESULT
import platform.windows.S_OK

/**
 * [IVectorView on MSDN](https://learn.microsoft.com/en-us/uwp/api/windows.foundation.collections.ivectorview-1?view=winrt-26100)
 */
@ExperimentalForeignApi
class IVectorView(
    typeArgs: List<RtType>
) : RtInterface<IVectorView.Companion>(Companion, typeArgs) {
    private typealias _GetAt = (self: COpaquePointer, index: uint32_t, item: COpaquePointer) -> HRESULT
    private typealias _GetSize = (self: COpaquePointer, size: CPointer<uint32_tVar>) -> HRESULT

    companion object : RtInterfaceType {
        override val rtTypeName: String = "Windows.Foundation.Collections.IVectorView"
        override val arity: Int = 1

        override val functions: List<String> = VTableFunctionList.buildFor<IVectorView> {
            add("GetAt")
            add("GetSize")
            addStubs(2)
        }

        override fun create(typeArgs: List<RtType>): RtInterface<*> = IVectorView(typeArgs)
    }

    private val GetAt: CPointer<CFunction<_GetAt>> by vTable
    private val GetSize: CPointer<CFunction<_GetSize>> by vTable

    val size: Int
        get() = memScoped {
            val value = alloc<uint32_tVar>()
            GetSize(address, value.ptr)
            value.value.toInt()
        }

    inline val indices: IntRange
        get() = 0..<size

    fun <T : CVariable> getAt(index: Int, value: CPointer<T>): HRESULT {
        return GetAt(address, index.toUInt(), value)
    }

    inline fun <reified T : CVariable> asSequence(): Sequence<T> = sequence {
        for (index in indices) memScoped {
            val value = alloc<T>()
            if (getAt(index, value.ptr) != S_OK) continue
            yield(value)
        }
    }

    fun <I : RtInterface<T>, T : RtInterfaceType> asRtSequence(type: T, vararg typeArgs: RtType): Sequence<I> =
        asSequence<COpaquePointerVar>().mapNotNull { ptr -> ptr.value?.asRt(type, *typeArgs) }
}
