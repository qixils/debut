package dev.qixils.debut

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

interface JsonSerializable {
    fun toJson(): JsonElement
}

fun Collection<*>.toJsonElement(): JsonElement = JsonArray(mapNotNull { it.toJsonElement() })

fun Map<*, *>.toJsonElement(): JsonElement = JsonObject(
    mapNotNull {
        (it.key as? String ?: return@mapNotNull null) to it.value.toJsonElement()
    }.toMap(),
)

@OptIn(ExperimentalSerializationApi::class)
fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonSerializable -> toJson()
    is Map<*, *> -> toJsonElement()
    is Collection<*> -> toJsonElement()
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is UByte -> JsonPrimitive(this)
    is UShort -> JsonPrimitive(this)
    is UInt -> JsonPrimitive(this)
    is ULong -> JsonPrimitive(this)
    else -> JsonPrimitive(toString())
}