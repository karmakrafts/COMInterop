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

@file:OptIn(ExperimentalForeignApi::class)

package dev.karmakrafts.cominterop

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.sizeOf
import kotlin.test.Test
import kotlin.test.assertEquals

class HSTRINGTest : ComTest() {
    @Test
    fun scopedHstringReferencesRemainIndependent() = memScoped {
        val first = hstring("first \uD83D\uDE80")
        val second = hstring("second 日本語")
        assertEquals("first \uD83D\uDE80", ComRuntime.getString(first))
        assertEquals("second 日本語", ComRuntime.getString(second))
    }

    @Test
    fun headerHasTheWindowsX64AbiSize() {
        assertEquals(24L, sizeOf<HSTRING_HEADER>())
    }
}