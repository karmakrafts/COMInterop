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

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Representation of a WinRT type signature component.
 */
@ExperimentalForeignApi
interface RtType {
    /**
     * Runtime name used in WinRT metadata signatures.
     */
    val rtTypeName: String

    /**
     * Whether this type is fully instantiated.
     */
    val isInstantiated: Boolean

    /**
     * Generic arity for open generic type definitions.
     */
    val arity: Int get() = 0 // Arity is 0 for all default concrete types
}

/**
 * Concrete instantiation of a generic WinRT interface type.
 *
 * @param type Generic interface type definition.
 * @param args Concrete generic type arguments.
 */
@ExperimentalForeignApi
data class InstantiatedRtType(
    val type: RtInterfaceType, val args: List<RtType>
) : RtInterfaceType, RtType by type {
    companion object {
        private fun computeNameRecursively(type: RtType): String {
            require(type.isInstantiated) { "Type must be instantiated" }
            if (type is InstantiatedRtType) {
                var name = type.type.rtTypeName
                val args = type.args
                if (args.isEmpty()) return name
                name += "`${args.size}<"
                name += args.joinToString(transform = ::computeNameRecursively)
                name += '>'
                return name
            }
            return type.rtTypeName
        }
    }

    /**
     * ABI function layout inherited from [type].
     */
    override val functions: List<String> get() = type.functions

    /**
     * Number of generic arguments bound by this instantiation.
     */
    override val arity: Int get() = args.size

    /**
     * Fully rendered WinRT metadata type name.
     */
    override val rtTypeName: String by lazy { computeNameRecursively(this) }

    /**
     * Whether all generic arguments are instantiated.
     */
    override val isInstantiated: Boolean by lazy {
        if (args.isEmpty()) true // If we don't have any type args, the type is considered instantiated
        else args.all(RtType::isInstantiated)
    }

    /**
     * Creates an interface instance from the wrapped type definition.
     *
     * @param typeArgs Generic type arguments passed to [type].
     * @return Created WinRT interface wrapper.
     */
    override fun create(typeArgs: List<RtType>): RtInterface<*> = type.create(typeArgs)
}

/**
 * Base class for primitive WinRT metadata types.
 *
 * @param rtTypeName Runtime metadata name.
 */
@ExperimentalForeignApi
sealed class PrimitiveRtType(override val rtTypeName: String) : RtType {
    /**
     * Primitive types are always concrete.
     */
    override val isInstantiated: Boolean = true
}

// All primitive types (and builtin structs) supported out of the box
@ExperimentalForeignApi
object RtInt16 : PrimitiveRtType("Int16")

@ExperimentalForeignApi
object RtInt32 : PrimitiveRtType("Int32")

@ExperimentalForeignApi
object RtInt64 : PrimitiveRtType("Int64")

@ExperimentalForeignApi
object RtUInt16 : PrimitiveRtType("UInt16")

@ExperimentalForeignApi
object RtUInt32 : PrimitiveRtType("UInt32")

@ExperimentalForeignApi
object RtUInt64 : PrimitiveRtType("UInt64")

@ExperimentalForeignApi
object RtSingle : PrimitiveRtType("Single")

@ExperimentalForeignApi
object RtDouble : PrimitiveRtType("Double")

@ExperimentalForeignApi
object RtBoolean : PrimitiveRtType("Boolean")

@ExperimentalForeignApi
object RtChar16 : PrimitiveRtType("Char16")

@ExperimentalForeignApi
object RtGuid : PrimitiveRtType("Guid")

@ExperimentalForeignApi
object RtString : PrimitiveRtType("String")