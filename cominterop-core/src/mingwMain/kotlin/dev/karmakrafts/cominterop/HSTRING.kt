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

import kotlinx.cinterop.COpaque
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CStructVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alignOf
import kotlin.native.internal.NativePtr

@ExperimentalForeignApi
typealias HSTRING_IMPL = COpaque

@ExperimentalForeignApi
typealias HSTRING = CPointer<HSTRING_IMPL>

@ExperimentalForeignApi
typealias HSTRINGVar = CPointerVarOf<HSTRING>

// Docs state that this is 20 bytes on 32-bits, but if we ever compile for Windows x86, we over-allocate a bit
@ExperimentalForeignApi
class HSTRING_HEADER(rawPtr: NativePtr) : CStructVar(rawPtr) {
    @Suppress("DEPRECATION")
    companion object : Type(24L, alignOf<CPointerVar<*>>())
}