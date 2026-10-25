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

@file:OptIn(ExperimentalForeignApi::class, ExperimentalUnsignedTypes::class)

package dev.karmakrafts.cominterop.win32

import dev.karmakrafts.cominterop.ComTest
import dev.karmakrafts.cominterop.IUnknown
import dev.karmakrafts.cominterop.assertComDescriptor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class IMetaDataImportTest : ComTest() {
    @Test
    fun descriptorPreservesTheMetadataImportAbiSlots() {
        assertEquals(listOf(IUnknown), IMetaDataImport.superInterfaces.toList())
        assertIs<IMetaDataImport>(IMetaDataImport.create())
        assertComDescriptor(IMetaDataImport, "{7DAC8207-D3AE-4C75-9B67-92801A497D44}", 65)
        assertEquals("CloseEnum", IMetaDataImport.functions[0])
        assertEquals("FindTypeDefByName", IMetaDataImport.functions[6])
        assertEquals("GetTypeDefProps", IMetaDataImport.functions[9])
        assertEquals("GetCustomAttributeByName", IMetaDataImport.functions[57])
    }

    @Test
    fun readsInterfacePropertiesAndGuidAttribute() {
        val name = "Windows.Foundation.Collections.IPropertySet"
        withMetaDataImport(name) { import2, token ->
            val import: IMetaDataImport = import2.asCom(IMetaDataImport)
            import.use {
                assertEquals(token, it.findTypeDefByName(name, MDTOKEN_NIL))
                val props = assertNotNull(it.getTypeDefProps(token))
                assertEquals(name, props.typeName)
                assertEquals(TD_INTERFACE_MASK, props.flags and TD_CLASS_SEMANTICS_MASK)
                assertEquals(0x01000000U, props.extends, "Interfaces have an mdTypeRefNil base token")
                val blob = assertNotNull(
                    it.getCustomAttributeByName(
                        token, "Windows.Foundation.Metadata.GuidAttribute"
                    )
                )
                assertEquals(20, blob.size)
                assertEquals(1.toUByte(), blob[0])
                assertEquals(0.toUByte(), blob[1])
            }
        }
    }

    @Test
    fun missingTypesAndAttributesReturnNull() {
        withMetaDataImport("Windows.Foundation.Collections.IPropertySet") { import2, token ->
            val import: IMetaDataImport = import2.asCom(IMetaDataImport)
            import.use {
                assertNull(it.findTypeDefByName("COMInterop.Tests.DoesNotExist", MDTOKEN_NIL))
                assertNull(it.getTypeDefProps(MDTOKEN_NIL))
                assertNull(it.getCustomAttributeByName(token, "COMInterop.Tests.MissingAttribute"))
            }
        }
    }
}