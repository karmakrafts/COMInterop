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

package dev.karmakrafts.cominterop.win32

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import platform.windows.DWORD
import platform.windows.HRESULT
import platform.windows.UINT32
import platform.windows.UINT32Var

@ExperimentalForeignApi
internal typealias MdToken = UINT32

@ExperimentalForeignApi
internal typealias MdTokenVar = UINT32Var

@ExperimentalForeignApi
internal typealias MdTypeDef = MdToken

@ExperimentalForeignApi
internal typealias MdTypeDefVar = MdTokenVar

@ExperimentalForeignApi
internal typealias MdGenericParam = MdToken

@ExperimentalForeignApi
internal typealias MdGenericParamVar = MdTokenVar

@ExperimentalForeignApi
internal typealias HCORENUM = COpaquePointer

@ExperimentalForeignApi
internal typealias HCORENUMVar = COpaquePointerVar

@ExperimentalForeignApi
internal const val MDTOKEN_NIL: MdToken = 0U

@ExperimentalForeignApi
internal const val TD_CLASS_SEMANTICS_MASK: DWORD = 0x0000_0020U

@ExperimentalForeignApi
internal const val TD_INTERFACE_MASK: DWORD = 0x0000_0020U

@ExperimentalForeignApi
internal const val RO_E_METADATA_NAME_NOT_FOUND: HRESULT = 0x8000000F.toInt()

@ExperimentalForeignApi
internal const val RO_E_METADATA_NAME_IS_NAMESPACE: HRESULT = 0x80000010.toInt()

@ExperimentalForeignApi
internal const val RO_E_METADATA_INVALID_TYPE_FORMAT: HRESULT = 0x80000011.toInt()
