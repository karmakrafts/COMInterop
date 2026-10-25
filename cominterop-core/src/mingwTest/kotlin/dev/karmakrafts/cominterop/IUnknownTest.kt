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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class IUnknownTest {
    @Test
    fun unknownIsTheRootInterface() {
        assertTrue(IUnknown.superInterfaces.isEmpty())
        assertTrue(IUnknown.allSuperInterfaces().isEmpty())
        assertEquals(listOf("QueryInterface", "AddRef", "Release"), IUnknown.functions)
        assertIs<IUnknown>(IUnknown.create())
        assertComDescriptor(IUnknown, "{00000000-0000-0000-C000-000000000046}", 3)
    }
}