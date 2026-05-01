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
import dev.karmakrafts.cominterop.ComInterfaceType
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.IID

/**
 * Type descriptor for WinRT interfaces.
 */
@ExperimentalForeignApi
interface RtInterfaceType : ComInterfaceType, RtType {
    /**
     * Super interfaces inherited by WinRT interfaces.
     */
    override val superInterfaces: Array<ComInterfaceType>
        get() = arrayOf(IInspectable)

    /**
     * Indicates whether this type descriptor already represents a concrete instantiation.
     */
    override val isInstantiated: Boolean
        get() = false // RT interface types are not instantiated by default

    /**
     * Creates an interface wrapper for the given generic arguments.
     *
     * @param typeArgs Generic type arguments.
     * @return Newly created interface wrapper.
     */
    fun create(typeArgs: List<RtType>): RtInterface<*>

    /**
     * Creates a non-generic interface wrapper.
     *
     * @return Newly created interface wrapper.
     */
    override fun create(): ComInterface<*> = create(emptyList())

    /**
     * Resolves and writes the IID for [iface] into [iid].
     *
     * @param iid Destination pointer receiving the resolved IID.
     * @param iface Interface instance used for type resolution.
     */
    override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) {
        val rtIface = iface as RtInterface<*>
        RtInterfaceIdResolver.resolve(rtIface.instantiatedType, iid)
    }

    /**
     * Creates an instantiated WinRT type from this descriptor and [typeArgs].
     *
     * @param typeArgs Generic type arguments.
     * @return Instantiated WinRT type.
     */
    fun instantiate(typeArgs: List<RtType>): InstantiatedRtType = InstantiatedRtType(this, typeArgs)

    /**
     * Convenience operator to instantiate this interface type.
     *
     * @param typeArgs Generic type arguments.
     * @return Instantiated WinRT type.
     */
    operator fun get(vararg typeArgs: RtType): InstantiatedRtType = instantiate(typeArgs.toList())
}