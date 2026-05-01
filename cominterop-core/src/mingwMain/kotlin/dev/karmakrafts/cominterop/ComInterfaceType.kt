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

package dev.karmakrafts.cominterop

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.IID

/**
 * Describes metadata and factory behavior for a COM interface type.
 */
@ExperimentalForeignApi
interface ComInterfaceType {
    /**
     * Direct parent interfaces of this interface type.
     *
     * Defaults to [IUnknown].
     */
    val superInterfaces: Array<ComInterfaceType> get() = arrayOf(IUnknown)

    /**
     * Ordered list of function names defined by this interface type.
     */
    val functions: List<String>

    /**
     * Creates a new [ComInterface] wrapper instance for this interface type.
     */
    fun create(): ComInterface<*>

    /**
     * Writes this interface type's IID into [iid] for the provided [iface].
     */
    fun getIID(iid: CPointer<IID>, iface: ComInterface<*>)

    /**
     * Returns all transitive super interfaces in declaration order.
     */
    fun allSuperInterfaces(): List<ComInterfaceType> {
        val queue = ArrayDeque<ComInterfaceType>()
        queue += superInterfaces
        val visited = ArrayList<ComInterfaceType>()
        while (!queue.isEmpty()) {
            val iface = queue.removeFirst()
            queue += iface.superInterfaces
            if (iface in visited) continue
            visited += iface
        }
        return visited.reversed()
    }

    /**
     * Returns the total number of functions including all inherited interface functions.
     */
    fun getTotalFunctionCount(): Int = functions.size + allSuperInterfaces().sumOf { iface -> iface.functions.size }
}
