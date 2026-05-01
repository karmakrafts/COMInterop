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

import dev.karmakrafts.cominterop.dll.ComBase
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.CLSID
import platform.posix.IID
import platform.windows.CLSCTX_INPROC_SERVER
import platform.windows.DWORD
import platform.windows.LPVOIDVar
import platform.windows.S_OK

/**
 * Descriptor for a COM class that can expose and instantiate a default COM interface.
 *
 * @param I The [ComInterfaceType] describing the class' default interface.
 * @property defaultInterface The interface descriptor used to create and initialize interface wrappers.
 */
@ExperimentalForeignApi
interface ComClass<I : ComInterfaceType> {
    /**
     * The interface descriptor used by this class as its default COM interface.
     */
    val defaultInterface: I

    /**
     * Writes this class' COM class identifier (CLSID) into [clsid].
     *
     * @param clsid Pointer receiving the CLSID for this COM class.
     */
    fun getCLSID(clsid: CPointer<CLSID>)
}

/**
 * Instantiates this COM class and returns a wrapper for its default interface.
 *
 * @param I The concrete [ComInterface] wrapper type to return.
 * @param T The [ComInterfaceType] implemented by the returned wrapper.
 * @param C The [ComClass] receiver type providing class metadata.
 * @param clsContext The COM class context flags forwarded to `CoCreateInstance`.
 * @return A newly initialized COM interface wrapper.
 */
@ExperimentalForeignApi
@Suppress("UNCHECKED_CAST")
fun <I : ComInterface<T>, T : ComInterfaceType, C : ComClass<T>> C.new(
    clsContext: DWORD = CLSCTX_INPROC_SERVER.convert()
): I = memScoped {
    val obj = defaultInterface.create() as I
    val address = alloc<LPVOIDVar>()
    val clsid = alloc<CLSID>()
    val iid = alloc<IID>()
    getCLSID(clsid.ptr)
    defaultInterface.getIID(iid.ptr, obj)
    val result = ComBase.CoCreateInstance(clsid.ptr, null, clsContext, iid.ptr, address.ptr)
    check(result == S_OK) {
        "COM object instantiation failed with CoCreateInstance: 0x${result.toHexString()}"
    }
    obj.init(requireNotNull(address.value) { "Could not create COM object for class ${this@new}" })
    obj
}
