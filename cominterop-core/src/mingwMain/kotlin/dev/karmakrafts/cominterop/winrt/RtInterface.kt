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

/**
 * Base wrapper for WinRT interfaces.
 *
 * @param T Interface descriptor type.
 * @param interfaceType Interface type descriptor instance.
 * @param typeArgs Generic type arguments used to instantiate [interfaceType].
 */
@ExperimentalForeignApi
abstract class RtInterface<T : RtInterfaceType>(
    interfaceType: T, typeArgs: List<RtType>
) : ComInterface<T>(interfaceType) {
    /**
     * Fully instantiated WinRT type represented by this interface instance.
     */
    val instantiatedType: InstantiatedRtType = interfaceType.instantiate(typeArgs)

    /**
     * Queries this object for another WinRT interface and wraps it as [I].
     *
     * @param I Resulting WinRT interface wrapper type.
     * @param RT Interface descriptor type for [type].
     * @param type Target interface descriptor to query.
     * @param typeArgs Generic type arguments for [type].
     * @return Queried interface wrapper.
     */
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

/**
 * Wraps a raw COM pointer as a WinRT interface.
 *
 * @param I Resulting WinRT interface wrapper type.
 * @param T Interface descriptor type.
 * @param type Interface descriptor used to instantiate the wrapper.
 * @param typeArgs Generic type arguments for [type].
 * @return Wrapped interface instance initialized with this pointer.
 */
@ExperimentalForeignApi
@Suppress("UNCHECKED_CAST")
fun <I : RtInterface<T>, T : RtInterfaceType> CPointer<*>.asRt(type: T, vararg typeArgs: RtType): I {
    check(type.arity == typeArgs.size) { "WinRT type $type requires ${type.arity} type arguments" }
    return (type.create(typeArgs.toList()) as I).apply { init(this@asRt) }
}
