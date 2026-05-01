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
import dev.karmakrafts.cominterop.winrt.collections.IIterable.Companion
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.HRESULT
import platform.windows.S_OK

/**
 * [IIterable on MSDN](https://learn.microsoft.com/en-us/uwp/api/windows.foundation.collections.iiterable-1?view=winrt-26100)
 *
 * @param typeArgs Generic type arguments, where the first argument is the element type.
 */
@ExperimentalForeignApi
class IIterable(
    typeArgs: List<RtType>
) : RtInterface<Companion>(Companion, typeArgs) {
    private typealias _First = (self: COpaquePointer, first: CPointer<COpaquePointerVar>) -> HRESULT

    companion object : RtInterfaceType {
        override val functions: List<String> = VTableFunctionList.buildFor<IIterable> { add("First") }
        override val rtTypeName: String = "Windows.Foundation.Collections.IIterable"
        override val arity: Int = 1
        override fun create(typeArgs: List<RtType>): RtInterface<*> = IIterable(typeArgs)
    }

    private val First: CPointer<CFunction<_First>> by vTable

    /** Retrieves the first iterator for this iterable. */
    val first: IIterator
        get() = memScoped {
            val address = alloc<COpaquePointerVar>()
            check(First(this@IIterable.address, address.ptr) == S_OK) {
                "Could not obtain first iterator from iterable"
            }
            requireNotNull(address.value) {
                "Could not obtain first iterator address"
            }.asRt(IIterator, instantiatedType.args.first()) // IIterator has the same type as its IIterable
        }
}

