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

/**
 * Base wrapper for native COM interface pointers that resolves callable entries from a v-table.
 *
 * @param functionCount Total number of function entries expected in this interface v-table.
 */
@ExperimentalForeignApi
abstract class VTableInterface(
    private val functionCount: Int
) {
    /**
     * Native COM interface pointer (`void**`) backing this wrapper.
     *
     * Initialized by [init].
     */
    lateinit var address: COpaquePointer
        private set

    private val vTableAddress: COpaquePointer by lazy {
        requireNotNull(address.reinterpret<CPointerVar<COpaquePointerVar>>()[0]) {
            "Could not retrieve v-table address of interface"
        }
    }

    /**
     * Lazily created [VTable] wrapper for this interface.
     */
    protected val vTable: Lazy<VTable> = lazy { VTable(vTableAddress, functionCount) }

    /**
     * Resolves a v-table function by delegated [property] name.
     *
     * @param F Kotlin function signature represented by the resulting C function pointer.
     * @param thisRef Owning delegated instance (unused).
     * @param property Delegated property used to determine the function name.
     * @return Typed C function pointer resolved from [property].
     */
    protected operator fun <F : Function<*>> Lazy<VTable>.getValue(
        thisRef: Any?, property: KProperty<*>
    ): CPointer<CFunction<F>> = value[property.name]

    /**
     * Populates [vTable] with the function names used by this interface wrapper.
     *
     * @param vTable V-table mapping to populate.
     */
    protected abstract fun populateVTable(vTable: VTable)

    /**
     * Initializes this wrapper with a native COM interface [address] and populates its v-table mapping.
     *
     * @param address Native COM interface pointer to wrap.
     */
    fun init(address: COpaquePointer) {
        this.address = address
        populateVTable(vTable.value)
    }
}
