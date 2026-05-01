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

package dev.karmakrafts.cominterop.winrt

import dev.karmakrafts.cominterop.ComInterface
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.IID

@ExperimentalForeignApi
abstract class RtInterface<T : RtInterfaceType>(
    interfaceType: T, typeArgs: List<RtType>
) : ComInterface<T>(interfaceType) {
    val instantiatedType: InstantiatedRtType = interfaceType.instantiate(typeArgs)

    @Suppress("UNCHECKED_CAST")
    fun <I : RtInterface<RT>, RT : RtInterfaceType> asRt(type: RT, vararg typeArgs: RtType): I = memScoped {
        check(type.arity == typeArgs.size) { "WinRT type $type requires ${type.arity} type arguments" }
        val iface = type.create(typeArgs.toList()) as I
        val address = alloc<COpaquePointerVar>()
        val iid = alloc<IID>()
        type.getIID(iid.ptr, iface)
        queryInterface(iid.ptr, address.ptr)
        iface.init(requireNotNull(address.value) { "Could not retrieve interface $iid from instance" })
        iface
    }
}

@ExperimentalForeignApi
@Suppress("UNCHECKED_CAST")
internal fun <I : RtInterface<T>, T : RtInterfaceType> CPointer<*>.asRt(type: T, vararg typeArgs: RtType): I {
    check(type.arity == typeArgs.size) { "WinRT type $type requires ${type.arity} type arguments" }
    return (type.create(typeArgs.toList()) as I).apply { init(this@asRt) }
}
