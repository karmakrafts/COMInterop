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

import dev.karmakrafts.cominterop.vtable.VTable
import dev.karmakrafts.cominterop.vtable.VTableInterface
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.IID
import platform.windows.HRESULT
import platform.windows.ULONG

@ExperimentalForeignApi
abstract class ComInterface<T : ComInterfaceType>( // @formatter:off
    val interfaceType: T
) : VTableInterface(interfaceType.getTotalFunctionCount()), AutoCloseable { // @formatter:on
    private typealias _QueryInterface = (self: COpaquePointer, iid: CPointer<IID>, ppvObject: CPointer<COpaquePointerVar>) -> HRESULT
    private typealias _AddRef = (self: COpaquePointer) -> ULONG
    private typealias _Release = (self: COpaquePointer) -> ULONG

    protected val QueryInterface: CPointer<CFunction<_QueryInterface>> by vTable
    protected val AddRef: CPointer<CFunction<_AddRef>> by vTable
    protected val Release: CPointer<CFunction<_Release>> by vTable

    override fun populateVTable(vTable: VTable) {
        // First insert all v-table entries from all super interfaces in order
        for (superIface in interfaceType.allSuperInterfaces()) {
            vTable += superIface.functions
        }
        // Then insert the functions of the actual interface type
        vTable += interfaceType.functions
    }

    fun queryInterface(iid: CPointer<IID>, ppvObject: CPointer<COpaquePointerVar>): HRESULT {
        return QueryInterface(address, iid, ppvObject)
    }

    fun addRef(): ULONG = AddRef(address)
    fun release(): ULONG = Release(address)

    @Suppress("UNCHECKED_CAST")
    fun <I : ComInterface<T>, T : ComInterfaceType> asCom(type: T): I = memScoped {
        val iface = type.create() as I
        val address = alloc<COpaquePointerVar>()
        val iid = alloc<IID>()
        type.getIID(iid.ptr, iface)
        queryInterface(iid.ptr, address.ptr)
        iface.init(requireNotNull(address.value) { "Could not retrieve interface $iid from instance" })
        iface
    }

    override fun close() {
        release()
    }
}

@ExperimentalForeignApi
@Suppress("UNCHECKED_CAST")
fun <I : ComInterface<T>, T : ComInterfaceType> CPointer<*>.asCom(iface: T): I {
    return (iface.create() as I).apply { init(this@asCom) }
}
