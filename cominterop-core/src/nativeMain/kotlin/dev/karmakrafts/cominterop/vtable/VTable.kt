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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlin.reflect.KProperty

/**
 * A v-table implementation which allows defining functions at runtime,
 * as well as retrieving their address by name.
 *
 * @param baseAddress Base native address of the v-table entry array.
 * @param functionCount Total number of function slots available in this v-table.
 */
@ExperimentalForeignApi
class VTable(
    baseAddress: COpaquePointer, private val functionCount: Int
) {
    private val entries: CPointer<COpaquePointerVar> = baseAddress.reinterpret()
    private val functions: ArrayList<String> = ArrayList()

    /**
     * Registers a function [name] in this v-table without assigning an address yet.
     *
     * The function order defines the final slot index in the table.
     *
     * @param name Function name to register.
     */
    operator fun plusAssign(name: String) {
        require(functions.size < functionCount) { "V-table cannot fit additional function '$name'" }
        require(name !in functions) { "V-table already contains function '$name'" }
        functions += name
    }

    /**
     * Registers all [names] in declaration order.
     *
     * @param names Function names to register.
     */
    operator fun plusAssign(names: Collection<String>) {
        names.forEach(::plusAssign)
    }

    /**
     * Registers [name] and immediately assigns [address] to its slot in the v-table.
     *
     * @param name Function name to register.
     * @param address Native function pointer assigned to the function slot.
     */
    operator fun set(name: String, address: COpaquePointer) {
        require(functions.size < functionCount) { "V-table cannot fit additional function '$name'" }
        require(name !in functions) { "V-table already contains function '$name'" }
        entries[functions.size] = address
        if (name !in functions) functions += name
    }

    /**
     * Resolves the native function pointer address for a previously registered [name].
     *
     * @param name Registered function name to resolve.
     * @return Native function pointer stored in the slot of [name].
     */
    fun getAddress(name: String): COpaquePointer {
        require(name in functions) { "No function named '$name' in v-table" }
        val index = functions.indexOf(name)
        return requireNotNull(entries[index]) {
            "Could not retrieve v-table offset for function '$name'"
        }
    }

    /**
     * Resolves and reinterprets the entry for [name] as a typed C function pointer.
     *
     * @param F Kotlin function signature represented by the resulting C function pointer.
     * @param name Registered function name to resolve.
     * @return Typed C function pointer for [name].
     */
    operator fun <F : Function<*>> get(name: String): CPointer<CFunction<F>> = getAddress(name).reinterpret()

    /**
     * Property delegate variant of [get] that resolves by [property] name.
     *
     * @param F Kotlin function signature represented by the resulting C function pointer.
     * @param thisRef Owning delegated instance (unused).
     * @param property Delegated property used to determine the function name.
     * @return Typed C function pointer resolved from [property].
     */
    operator fun <F : Function<*>> getValue(thisRef: Any?, property: KProperty<*>): CPointer<CFunction<F>> =
        get(property.name)
}
