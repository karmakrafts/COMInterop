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

@ExperimentalForeignApi
interface RtType {
    val rtTypeName: String
    val isInstantiated: Boolean
    val arity: Int get() = 0 // Arity is 0 for all default concrete types
}

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

    override val functions: List<String> get() = type.functions
    override val arity: Int get() = args.size
    override val rtTypeName: String by lazy { computeNameRecursively(this) }

    override val isInstantiated: Boolean by lazy {
        if (args.isEmpty()) true // If we don't have any type args, the type is considered instantiated
        else args.all(RtType::isInstantiated)
    }

    override fun create(typeArgs: List<RtType>): RtInterface<*> = type.create(typeArgs)
}

@ExperimentalForeignApi
sealed class PrimitiveRtType(override val rtTypeName: String) : RtType {
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