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

@ExperimentalForeignApi
interface ComInterfaceType {
    val superInterfaces: Array<ComInterfaceType> get() = arrayOf(IUnknown)
    val functions: List<String>

    fun create(): ComInterface<*>
    fun getIID(iid: CPointer<IID>, iface: ComInterface<*>)

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

    fun getTotalFunctionCount(): Int = functions.size + allSuperInterfaces().sumOf { iface -> iface.functions.size }
}
