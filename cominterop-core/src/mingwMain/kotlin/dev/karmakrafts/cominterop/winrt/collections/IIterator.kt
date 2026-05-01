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
import dev.karmakrafts.cominterop.winrt.collections.IIterator.Companion
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.uint32_t
import platform.posix.uint32_tVar
import platform.windows.HRESULT
import platform.windows.booleanVar

@ExperimentalForeignApi
class IIterator(
    typeArgs: List<RtType>
) : RtInterface<Companion>(Companion, typeArgs) {
    private typealias _GetCurrent = (self: COpaquePointer, current: COpaquePointer) -> HRESULT
    private typealias _GetHasCurrent = (self: COpaquePointer, hasCurrent: CPointer<booleanVar>) -> HRESULT
    private typealias _MoveNext = (self: COpaquePointer, hasCurrent: CPointer<booleanVar>) -> HRESULT
    private typealias _GetMany = (self: COpaquePointer, capacity: uint32_t, value: COpaquePointer, actual: CPointer<uint32_tVar>) -> HRESULT

    companion object : RtInterfaceType {
        override val rtTypeName: String = "Windows.Foundation.Collections.IIterator"
        override val arity: Int = 1

        override val functions: List<String> = VTableFunctionList.buildFor<IIterator> {
            add("GetCurrent")
            add("GetHasCurrent")
            add("MoveNext")
            add("GetMany")
        }

        override fun create(typeArgs: List<RtType>): RtInterface<*> = IIterator(typeArgs)
    }

    val GetCurrent: CPointer<CFunction<_GetCurrent>> by vTable
    val GetHasCurrent: CPointer<CFunction<_GetHasCurrent>> by vTable
    val MoveNext: CPointer<CFunction<_MoveNext>> by vTable
    val GetMany: CPointer<CFunction<_GetMany>> by vTable
}
