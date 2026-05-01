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

package dev.karmakrafts.cominterop.winrt

import dev.karmakrafts.cominterop.ComRuntime
import dev.karmakrafts.cominterop.HSTRINGVar
import dev.karmakrafts.cominterop.asCom
import dev.karmakrafts.cominterop.dll.ComBase
import dev.karmakrafts.cominterop.dll.WinTypes
import dev.karmakrafts.cominterop.hstring
import dev.karmakrafts.cominterop.win32.HCORENUMVar
import dev.karmakrafts.cominterop.win32.IMetaDataImport
import dev.karmakrafts.cominterop.win32.IMetaDataImport2
import dev.karmakrafts.cominterop.win32.MDTOKEN_NIL
import dev.karmakrafts.cominterop.win32.MdTypeDefVar
import dev.karmakrafts.cominterop.win32.RO_E_METADATA_INVALID_TYPE_FORMAT
import dev.karmakrafts.cominterop.win32.RO_E_METADATA_NAME_IS_NAMESPACE
import dev.karmakrafts.cominterop.win32.RO_E_METADATA_NAME_NOT_FOUND
import dev.karmakrafts.cominterop.win32.TD_CLASS_SEMANTICS_MASK
import dev.karmakrafts.cominterop.win32.TD_INTERFACE_MASK
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.cinterop.wcstr
import platform.posix.GUID
import platform.posix.IID
import platform.posix.atexit
import platform.posix.memcpy
import platform.windows.DWORDVar
import platform.windows.E_INVALIDARG
import platform.windows.E_NOTIMPL
import platform.windows.E_POINTER
import platform.windows.HRESULT
import platform.windows.PCWSTR
import platform.windows.PCWSTRVar
import platform.windows.S_OK

/**
 * Resolves interface identifiers for WinRT type signatures.
 */
@ExperimentalForeignApi
object RtInterfaceIdResolver {
    private const val GUID_ATTRIBUTE_NAME: String = "Windows.Foundation.Metadata.GuidAttribute"

    private val builtinTypes: Set<String> = listOf( // @formatter:off
        RtInt16, RtInt32, RtInt64,
        RtUInt16, RtUInt32, RtUInt64,
        RtSingle, RtDouble,
        RtChar16, RtBoolean, RtString, RtGuid
    ).map(RtType::rtTypeName).toSet() // @formatter:on

    private val locator: IRoMetaDataLocator = IRoMetaDataLocator(::locate)

    init {
        atexit(staticCFunction<Unit> {
            val self = RtInterfaceIdResolver
            self.locator.close() // Make sure we release the heap memory for the locator
        })
    }

    private fun locate(nameData: PCWSTR, destination: COpaquePointer): HRESULT {
        val name = nameData.toKStringFromUtf16()
        val builder = IRoSimpleMetaDataBuilder(destination)
        if (name in builtinTypes) { // Short path for builtin types
            builder.setPrimitive(name)
            return S_OK
        }
        return memScoped {
            val filePath = alloc<HSTRINGVar>()
            val importAddress = alloc<COpaquePointerVar>()
            val enclosingTypeDef = alloc<MdTypeDefVar>() // This is not used but needs to be allocated!
            var result = WinTypes.RoGetMetaDataFile(
                hstring(name), null, filePath.ptr, importAddress.ptr, enclosingTypeDef.ptr
            )
            if (result != S_OK) return@memScoped result

            val import2 = importAddress.value?.asCom<IMetaDataImport2, _>(IMetaDataImport2)
                ?: return@memScoped E_POINTER
            val import1 = import2.asCom<IMetaDataImport, _>(IMetaDataImport)

            val typeDef = import1.findTypeDefByName(name, MDTOKEN_NIL)
            if (typeDef == null) {
                import2.release()
                return@memScoped E_INVALIDARG
            }
            val props = import1.getTypeDefProps(typeDef)
            if (props == null) {
                import2.release()
                filePath.value?.let(ComRuntime::deleteString)
                return@memScoped E_INVALIDARG
            }
            if ((props.flags and TD_CLASS_SEMANTICS_MASK) != TD_INTERFACE_MASK) {
                import2.release()
                filePath.value?.let(ComRuntime::deleteString)
                return@memScoped E_NOTIMPL // We don't support anything but builtins and interfaces
            }

            val hEnum = alloc<HCORENUMVar>()
            val params = import2.enumGenericParams(hEnum.ptr, typeDef)
            hEnum.value?.let(import1::closeEnum)

            val attribData = import1.getCustomAttributeByName(typeDef, GUID_ATTRIBUTE_NAME)
            if (attribData == null) {
                import2.release()
                filePath.value?.let(ComRuntime::deleteString)
                return@memScoped E_INVALIDARG
            }
            val guid = alloc<GUID>()
            if (!extractGuidFromAttribData(attribData, guid.ptr)) return@memScoped E_INVALIDARG

            // When generic params are present, we need to use setParametrizedInterface to pass arity
            result = if (params.isNotEmpty()) builder.setParametrizedInterface(guid, params.size.toUInt())
            else builder.setWinRtInterface(guid)

            if (result != S_OK) {
                import2.release()
                filePath.value?.let(ComRuntime::deleteString)
                return@memScoped result
            }

            import2.release()
            filePath.value?.let(ComRuntime::deleteString)
            S_OK
        }
    }

    // Reverse engineered from the WinSDK implementation;
    // Takes our 20 bytes of attribute data and turns them into a valid GUID.
    private fun extractGuidFromAttribData(data: UByteArray, guid: CPointer<GUID>): Boolean {
        val prolog = data[0].toUInt() or (data[1].toUInt() shl 8)
        if (prolog != 1U) return false
        data.usePinned { pinnedData ->
            memcpy(guid, pinnedData.addressOf(2), sizeOf<GUID>().convert())
        }
        return true
    }

    private fun parseType(type: String): List<String> = memScoped {
        val count = alloc<DWORDVar>()
        val parts = alloc<CPointerVar<HSTRINGVar>>()
        check(WinTypes.RoParseTypeName(hstring(type), count.ptr, parts.ptr) == S_OK) {
            "Could not parse WinRT type signature $type"
        }
        // @formatter:off
        val partsList = (0..<count.value.toInt())
            .mapNotNull(parts.value!!::get)
            .map(ComRuntime::getString)
        // @formatter:on
        parts.value?.let(ComBase.CoTaskMemFree::invoke)
        partsList
    }

    private fun getResolveError(result: HRESULT): String = when (result) {
        S_OK -> "No error"
        E_INVALIDARG -> "Invalid argument"
        E_NOTIMPL -> "Not implemented"
        RO_E_METADATA_NAME_NOT_FOUND -> "Name not found"
        RO_E_METADATA_NAME_IS_NAMESPACE -> "Name is namespace"
        RO_E_METADATA_INVALID_TYPE_FORMAT -> "Invalid type format"
        else -> "Unknown error"
    }

    /**
     * Resolves the IID for an instantiated WinRT [type].
     *
     * @param type Instantiated WinRT type to resolve.
     * @param iid Destination pointer receiving the resolved IID.
     */
    fun resolve(type: RtType, iid: CPointer<IID>) = memScoped {
        val typeName = type.rtTypeName
        val typeParts = parseType(typeName) // Extract generic type we want to resolve
        check(typeParts.isNotEmpty()) { "Malformed WinRT type '$typeName'" }
        val nameElements = ComBase.CoTaskMemAlloc((sizeOf<PCWSTRVar>() * typeParts.size).toULong())
            .reinterpret<PCWSTRVar>()
        for (i in typeParts.indices) {
            nameElements[i] = typeParts[i].wcstr.ptr // Allocate parts themselves on the stack
        }
        val result = ComBase.RoGetParameterizedTypeInstanceIID(
            typeParts.size.toUInt(), nameElements, locator.address, iid, null
        )
        if (result != S_OK) {
            ComBase.CoTaskMemFree(nameElements)
            error("Could not resolve IID for WinRT type $typeName: ${getResolveError(result)}")
        }
        ComBase.CoTaskMemFree(nameElements)
    }
}
