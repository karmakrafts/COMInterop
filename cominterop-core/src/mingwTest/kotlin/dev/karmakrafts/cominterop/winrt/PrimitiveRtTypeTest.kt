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

package dev.karmakrafts.cominterop.winrt

import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimitiveRtTypeTest {
    @Test
    fun allPredefinedPrimitivesAreConcreteParameterlessTypes() {
        val types = listOf(
            RtInt16 to "Int16",
            RtInt32 to "Int32",
            RtInt64 to "Int64",
            RtUInt16 to "UInt16",
            RtUInt32 to "UInt32",
            RtUInt64 to "UInt64",
            RtSingle to "Single",
            RtDouble to "Double",
            RtBoolean to "Boolean",
            RtChar16 to "Char16",
            RtGuid to "Guid",
            RtString to "String"
        )
        for ([type, name] in types) {
            assertEquals(name, type.rtTypeName)
            assertTrue(type.isInstantiated, name)
            assertEquals(0, type.arity, name)
        }
    }
}