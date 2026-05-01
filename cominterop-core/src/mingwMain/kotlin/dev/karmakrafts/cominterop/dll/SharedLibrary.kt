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

package dev.karmakrafts.cominterop.dll

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import platform.windows.FreeLibrary
import platform.windows.GetProcAddress
import platform.windows.HMODULE
import platform.windows.LoadLibraryW
import kotlin.reflect.KProperty

@OptIn(ExperimentalForeignApi::class)
internal class SharedLibrary( // @formatter:off
    private val name: String,
    private val module: HMODULE
) : AutoCloseable { // @formatter:on
    companion object {
        fun open(vararg names: String): SharedLibrary? {
            var library: SharedLibrary? = null
            for (name in names) {
                val module = LoadLibraryW(name) ?: continue
                library = SharedLibrary(name, module)
                break
            }
            return library
        }
    }

    fun findFunctionOrNull(name: String): COpaquePointer? = GetProcAddress(module, name)

    fun <T : Function<*>> findFunctionOrNull(name: String): CPointer<CFunction<T>>? {
        return findFunctionOrNull(name)?.reinterpret()
    }

    fun <T : Function<*>> findFunction(name: String): CPointer<CFunction<T>> {
        return requireNotNull(findFunctionOrNull<T>(name)) { "Could not find function ${this.name}:$name" }
    }

    operator fun <F : Function<*>> getValue(thisRef: Any?, property: KProperty<*>): CPointer<CFunction<F>> {
        return findFunction(property.name)
    }

    override fun close() {
        FreeLibrary(module)
    }
}
