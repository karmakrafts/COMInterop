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

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlin.reflect.KProperty

@ExperimentalForeignApi
abstract class VTableInterface(
    private val functionCount: Int
) {
    lateinit var address: COpaquePointer
        private set

    private val vTableAddress: COpaquePointer by lazy {
        requireNotNull(address.reinterpret<CPointerVar<COpaquePointerVar>>()[0]) {
            "Could not retrieve v-table address of interface"
        }
    }

    protected val vTable: Lazy<VTable> = lazy { VTable(vTableAddress, functionCount) }

    protected operator fun <F : Function<*>> Lazy<VTable>.getValue(
        thisRef: Any?, property: KProperty<*>
    ): CPointer<CFunction<F>> = value[property.name]

    protected abstract fun populateVTable(vTable: VTable)

    fun init(address: COpaquePointer) {
        this.address = address
        populateVTable(vTable.value)
    }
}
