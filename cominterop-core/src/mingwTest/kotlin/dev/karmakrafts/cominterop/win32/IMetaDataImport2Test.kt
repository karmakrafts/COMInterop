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

package dev.karmakrafts.cominterop.win32

import dev.karmakrafts.cominterop.ComTest
import dev.karmakrafts.cominterop.IUnknown
import dev.karmakrafts.cominterop.assertComDescriptor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class IMetaDataImport2Test : ComTest() {
    @Test
    fun descriptorExtendsTheOriginalMetadataImport() {
        assertEquals(listOf(IMetaDataImport), IMetaDataImport2.superInterfaces.toList())
        assertEquals(listOf(IUnknown, IMetaDataImport), IMetaDataImport2.allSuperInterfaces())
        assertIs<IMetaDataImport2>(IMetaDataImport2.create())
        assertComDescriptor(IMetaDataImport2, "{FCE5EFA0-8BBA-4F8E-A036-8F2022B08466}", 73)
        assertEquals("EnumGenericParams", IMetaDataImport2.functions[0])
    }

    @Test
    fun enumeratesGenericParametersAndHandlesParameterlessTypes() {
        for ([name, count] in listOf("IMap`2" to 2, "IIterable`1" to 1, "IPropertySet" to 0)) {
            withMetaDataImport("Windows.Foundation.Collections.$name") { import2, token ->
                val import: IMetaDataImport = import2.asCom(IMetaDataImport)
                import.use {
                    memScoped {
                        val handle = alloc<HCORENUMVar> { value = null }
                        try {
                            val params = import2.enumGenericParams(handle.ptr, token)
                            assertEquals(count, params.size, name)
                            assertEquals(count, params.distinct().size, name)
                            assertTrue(params.all { it != MDTOKEN_NIL }, name)
                            assertTrue(import2.enumGenericParams(handle.ptr, token).isEmpty(), name)
                        }
                        finally {
                            handle.value?.let(import::closeEnum)
                        }
                    }
                }
            }
        }
    }
}