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

package dev.karmakrafts.cominterop.winrt.collections

import dev.karmakrafts.cominterop.ComInterface
import dev.karmakrafts.cominterop.ComRuntime
import dev.karmakrafts.cominterop.winrt.RtInterface
import dev.karmakrafts.cominterop.winrt.RtInterfaceType
import dev.karmakrafts.cominterop.winrt.RtType
import dev.karmakrafts.cominterop.winrt.collections.IPropertySet.Companion
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.IID

@ExperimentalForeignApi
class IPropertySet : RtInterface<Companion>(Companion, emptyList()) {
    companion object : RtInterfaceType {
        override val rtTypeName: String = "Windows.Foundation.Collections.IPropertySet"
        override val functions: List<String> = emptyList()
        override val isInstantiated: Boolean = true

        override fun create(typeArgs: List<RtType>): RtInterface<*> = IPropertySet()

        override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) {
            ComRuntime.iidFromString("{8A43ED9F-F4E6-4421-ACF9-1DAB2986820C}", iid)
        }
    }
}
