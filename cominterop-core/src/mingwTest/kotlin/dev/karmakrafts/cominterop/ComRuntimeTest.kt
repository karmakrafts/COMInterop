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
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.CLSID
import platform.posix.IID
import platform.windows.S_OK
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComRuntimeTest {
    @BeforeTest
    fun initRuntime() {
        ComRuntime.init()
    }

    @AfterTest
    fun uninitRuntime() {
        ComRuntime.uninit()
    }

    @Test
    fun iidParsingPopulatesAllFields() = memScoped {
        val iid = alloc<IID>()
        ComRuntime.iidFromString("{01234567-89AB-CDEF-0123-456789ABCDEF}", iid.ptr)
        assertEquals(0x01234567U, iid.Data1)
        assertEquals(0x89AB.toUShort(), iid.Data2)
        assertEquals(0xCDEF.toUShort(), iid.Data3)
        assertEquals("{01234567-89AB-CDEF-0123-456789ABCDEF}", ComRuntime.guidToString(iid))
    }

    @Test
    fun iidEqualityComparesValuesAndAcceptsTheSamePointer() = memScoped {
        val first = alloc<IID>()
        val second = alloc<IID>()
        ComRuntime.iidFromString("{00000000-0000-0000-C000-000000000046}", first.ptr)
        ComRuntime.iidFromString("{00000000-0000-0000-c000-000000000046}", second.ptr)
        assertTrue(ComRuntime.iidEquals(first.ptr, first.ptr))
        assertTrue(ComRuntime.iidEquals(first.ptr, second.ptr))
        assertTrue(ComRuntime.iidEquals(first.ptr, "{00000000-0000-0000-c000-000000000046}"))
        ComRuntime.iidFromString("{00000000-0000-0000-C000-000000000047}", second.ptr)
        assertFalse(ComRuntime.iidEquals(first.ptr, second.ptr))
        assertFalse(ComRuntime.iidEquals(first.ptr, "{00000000-0000-0000-C000-000000000047}"))
    }

    @Test
    fun iidParsingRejectsInvalidLengths() = memScoped {
        val iid = alloc<IID>()
        for (value in listOf("", "00000000-0000-0000-C000-000000000046", "{00000000-0000-0000-C000-000000000046} ")) {
            assertFailsWith<IllegalArgumentException> { ComRuntime.iidFromString(value, iid.ptr) }
        }
    }

    @Test
    fun iidParsingRejectsMalformedCanonicalLength(): Unit = memScoped {
        val iid = alloc<IID>()
        assertFailsWith<IllegalStateException> {
            ComRuntime.iidFromString("{GGGGGGGG-0000-0000-C000-000000000046}", iid.ptr)
        }
    }

    @Test
    fun stringIidComparisonRejectsInvalidInput(): Unit = memScoped {
        val iid = alloc<IID>()
        ComRuntime.iidFromString("{00000000-0000-0000-C000-000000000046}", iid.ptr)
        assertFailsWith<IllegalArgumentException> { ComRuntime.iidEquals(iid.ptr, "invalid") }
    }

    @Test
    fun guidCopyProducesAnIndependentValue() = memScoped {
        val source = alloc<IID>()
        val destination = alloc<IID>()
        ComRuntime.iidFromString("{01234567-89AB-CDEF-0123-456789ABCDEF}", source.ptr)
        ComRuntime.copy(destination.ptr, source.ptr)
        assertTrue(ComRuntime.iidEquals(source.ptr, destination.ptr))
        source.Data1 = 0U
        assertEquals("{01234567-89AB-CDEF-0123-456789ABCDEF}", ComRuntime.guidToString(destination))
        assertFalse(ComRuntime.iidEquals(source.ptr, destination.ptr))
    }

    @Test
    fun copySupportsPrimitiveCVariables() = memScoped {
        val source = alloc<IntVar> { value = Int.MIN_VALUE }
        val destination = alloc<IntVar> { value = Int.MAX_VALUE }
        ComRuntime.copy(destination.ptr, source.ptr)
        assertEquals(Int.MIN_VALUE, destination.value)
    }

    @Test
    fun hstringsRoundTripUtf16() {
        for (value in listOf("COM", "Grüße 日本語 \uD83D\uDE80", "x".repeat(4096))) {
            val handle = ComRuntime.createString(value)
            try {
                assertEquals(value, ComRuntime.getString(handle))
            }
            finally {
                assertEquals(S_OK, ComRuntime.deleteString(handle))
            }
        }
    }

    @Test
    fun scopedHstringReferencesRemainIndependent() = memScoped {
        val first = hstring("first \uD83D\uDE80")
        val second = hstring("second 日本語")
        assertEquals("first \uD83D\uDE80", ComRuntime.getString(first))
        assertEquals("second 日本語", ComRuntime.getString(second))
    }

    @Test
    fun progIdResolvesBuiltInShellClass() = memScoped {
        val clsid = alloc<CLSID>()
        ComRuntime.clsidFromProgId("Shell.Application", clsid.ptr)
        assertTrue(ComRuntime.iidEquals(clsid.ptr, "{13709620-C279-11CE-A49E-444553540000}"))
    }

    @Test
    fun nestedRuntimeInitializationCanBeBalanced() {
        ComRuntime.init()
        try {
            val handle = ComRuntime.createString("nested initialization")
            try {
                assertEquals("nested initialization", ComRuntime.getString(handle))
            }
            finally {
                ComRuntime.deleteString(handle)
            }
        }
        finally {
            ComRuntime.uninit()
        }
        val handle = ComRuntime.createString("outer initialization")
        try {
            assertEquals("outer initialization", ComRuntime.getString(handle))
        }
        finally {
            ComRuntime.deleteString(handle)
        }
    }
}