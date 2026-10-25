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

import dev.karmakrafts.cominterop.winrt.collections.IIterable
import dev.karmakrafts.cominterop.winrt.collections.IKeyValuePair
import dev.karmakrafts.cominterop.winrt.collections.IMap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class InstantiatedRtTypeTest {
    @Test
    fun nestedNamesIncludeArityAndAllArguments() {
        val type = IIterable[IKeyValuePair[RtString, RtInt32]]
        assertTrue(type.isInstantiated)
        assertEquals(1, type.arity)
        assertEquals(
            "Windows.Foundation.Collections.IIterable`1<Windows.Foundation.Collections.IKeyValuePair`2<String, Int32>>",
            type.rtTypeName
        )
        assertSame(IIterable.functions, type.functions)
    }

    @Test
    fun unresolvedArgumentsPreventNameRendering() {
        val type = IIterable[IMap]
        assertFalse(type.isInstantiated)
        assertFailsWith<IllegalArgumentException> { type.rtTypeName }
    }

    @Test
    fun parameterlessTypesHaveNoGenericSuffix() {
        val type = IInspectable.instantiate(emptyList())
        assertTrue(type.isInstantiated)
        assertEquals(0, type.arity)
        assertEquals("Object", type.rtTypeName)
        assertIs<IInspectable>(type.create(emptyList()))
    }
}