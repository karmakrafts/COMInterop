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

import dev.karmakrafts.cominterop.dll.ComBase
import dev.karmakrafts.cominterop.hstring
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.IID
import platform.windows.S_OK

@ExperimentalForeignApi
interface RtClass {
    val rtTypeName: String
}

@ExperimentalForeignApi
@Suppress("UNCHECKED_CAST")
fun <I : RtInterface<T>, C : RtClass, T : RtInterfaceType> C.activate(type: T, vararg typeArgs: RtType): I = memScoped {
    val obj = type.create(typeArgs.toList()) as I
    val address = alloc<COpaquePointerVar>()
    val iid = alloc<IID>()
    type.getIID(iid.ptr, obj)
    val name = hstring(rtTypeName)
    if (ComBase.RoGetActivationFactory(name, iid.ptr, address.ptr) != S_OK) {
        // If RoGetActivationFactory fails, we hope that the default activation provides the right interface
        ComBase.RoActivateInstance(name, address.ptr)
    }
    obj.init(requireNotNull(address.value) { "Could not activate RT class '$rtTypeName'" })
    obj
}
