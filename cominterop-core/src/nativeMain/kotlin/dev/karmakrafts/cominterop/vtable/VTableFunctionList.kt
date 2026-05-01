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

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Mutable builder for ordered v-table function names.
 */
@ExperimentalForeignApi
class VTableFunctionList @PublishedApi internal constructor(
    private val stubPrefix: String, @PublishedApi internal val delegate: ArrayList<String> = ArrayList()
) {
    companion object {
        /**
         * Builds a function-name list using a mutable DSL receiver.
         *
         * [stubPrefix] is prepended to auto-generated stub names.
         *
         * @param stubPrefix Prefix to include in generated stub names.
         * @param block Builder block that mutates the list.
         * @return Immutable list of function names in insertion order.
         */
        inline fun build(stubPrefix: String = "", block: VTableFunctionList.() -> Unit): List<String> {
            return VTableFunctionList(stubPrefix).apply(block).delegate
        }

        /**
         * Builds a function-name list and derives the stub prefix from [T]'s simple class name.
         *
         * @param T Type whose simple class name is used as stub prefix.
         * @param block Builder block that mutates the list.
         * @return Immutable list of function names in insertion order.
         */
        inline fun <reified T : Any> buildFor(block: VTableFunctionList.() -> Unit): List<String> {
            val prefix = requireNotNull(T::class.simpleName) {
                "Could not obtain stub function prefix for ${T::class}"
            }
            return build(prefix, block)
        }
    }

    private var stubIndex: Int = 0

    private fun getNextStubName(): String {
        val stubPrefix = if (this.stubPrefix.isBlank()) ""
        else "${this.stubPrefix}_"
        return "__${stubPrefix}stub${stubIndex++}"
    }

    /**
     * Appends [count] auto-generated stub function names.
     *
     * @param count Number of stub entries to append.
     */
    fun addStubs(count: Int) {
        for (index in 0..<count) delegate += getNextStubName()
    }

    /**
     * Appends [precedingStubs] generated stubs followed by the concrete function [name].
     *
     * @param name Concrete function name to append.
     * @param precedingStubs Number of generated stubs to append before [name].
     */
    fun add(name: String, precedingStubs: Int = 0) {
        addStubs(precedingStubs)
        delegate += name
    }

    /**
     * Appends all [names] in order.
     *
     * @param names Function names to append in their iteration order.
     */
    fun addAll(names: Collection<String>) = names.forEach(::add)
}
