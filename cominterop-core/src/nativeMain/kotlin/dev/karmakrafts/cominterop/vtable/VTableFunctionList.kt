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

@ExperimentalForeignApi
class VTableFunctionList @PublishedApi internal constructor(
    private val stubPrefix: String, @PublishedApi internal val delegate: ArrayList<String> = ArrayList()
) {
    companion object {
        inline fun build(stubPrefix: String = "", block: VTableFunctionList.() -> Unit): List<String> {
            return VTableFunctionList(stubPrefix).apply(block).delegate
        }

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

    fun addStubs(count: Int) {
        for (index in 0..<count) delegate += getNextStubName()
    }

    fun add(name: String, precedingStubs: Int = 0) {
        addStubs(precedingStubs)
        delegate += name
    }

    fun addAll(names: Collection<String>) = names.forEach(::add)
}
