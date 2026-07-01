package com.bdsoftware.idorm.core.network.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Serializer linh hoạt cho trường String? — tự động chuyển đổi
 * mọi JSON primitive (boolean, number, string) thành String.
 * Xử lý trường hợp backend trả về false/0 thay vì "" hoặc null.
 */
object FlexibleStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeString()

        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonPrimitive) {
            if (element.isString) {
                val content = element.content
                return content.ifBlank { null }
            }
            // boolean false, number 0, etc. → treat as null
            val content = element.content
            if (content == "false" || content == "0") return null
            return content
        }
        return null
    }

    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) {
            encoder.encodeString(value)
        } else {
            encoder.encodeString("")
        }
    }
}
