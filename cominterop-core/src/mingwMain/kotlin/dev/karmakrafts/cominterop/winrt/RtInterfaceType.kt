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

@ExperimentalForeignApi
interface RtInterfaceType : ComInterfaceType, RtType {
    override val superInterfaces: Array<ComInterfaceType>
        get() = arrayOf(IInspectable)

    override val isInstantiated: Boolean
        get() = false // RT interface types are not instantiated by default

    fun create(typeArgs: List<RtType>): RtInterface<*>

    override fun create(): ComInterface<*> = create(emptyList())

    override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) {
        val rtIface = iface as RtInterface<*>
        RtInterfaceIdResolver.resolve(rtIface.instantiatedType, iid)
    }

    fun instantiate(typeArgs: List<RtType>): InstantiatedRtType = InstantiatedRtType(this, typeArgs)
    operator fun get(vararg typeArgs: RtType): InstantiatedRtType = instantiate(typeArgs.toList())
}