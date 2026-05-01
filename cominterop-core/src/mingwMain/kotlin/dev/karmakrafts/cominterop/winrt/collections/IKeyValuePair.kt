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
import dev.karmakrafts.cominterop.winrt.collections.IKeyValuePair.Companion
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import platform.windows.HRESULT

/**
 * [IKeyValuePair on MSDN](https://learn.microsoft.com/en-us/uwp/api/windows.foundation.collections.ikeyvaluepair-2?view=winrt-26100)
 */
@ExperimentalForeignApi
class IKeyValuePair(typeArgs: List<RtType>) : RtInterface<Companion>(Companion, typeArgs) {
    private typealias _GetKey = (self: COpaquePointer, key: COpaquePointer) -> HRESULT
    private typealias _GetValue = (self: COpaquePointer, value: COpaquePointer) -> HRESULT

    companion object : RtInterfaceType {
        override val rtTypeName: String = "Windows.Foundation.Collections.IKeyValuePair"
        override val arity: Int = 2

        override val functions: List<String> = VTableFunctionList.buildFor<IKeyValuePair> {
            add("GetKey")
            add("GetValue")
        }

        override fun create(typeArgs: List<RtType>): RtInterface<*> = IKeyValuePair(typeArgs)
    }

    private val GetKey: CPointer<CFunction<_GetKey>> by vTable
    private val GetValue: CPointer<CFunction<_GetValue>> by vTable

    fun <T : CPointed> getKey(key: CPointer<T>): HRESULT {
        return GetKey(address, key)
    }

    fun <T : CPointed> getValue(value: CPointer<T>): HRESULT {
        return GetValue(address, value)
    }
}
