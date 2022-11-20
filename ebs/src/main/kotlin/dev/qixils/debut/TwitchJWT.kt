@file:OptIn(ExperimentalSerializationApi::class)

package dev.qixils.debut

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.Payload
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.*
import java.util.*

enum class Role(
    val id: String,
    val isModerator: Boolean,
) {
    BROADCASTER("broadcaster", true),
    MODERATOR("moderator", true),
    VIEWER("viewer", false),
    EXTERNAL("external", false),
    ;

    companion object {
        fun fromId(id: String) = values().first { it.id == id }
    }
}

data class PubsubPerms
@JsonCreator constructor(

    @JsonProperty("send", required = false)
    val send: List<String>? = null,

    @JsonProperty("listen", required = false)
    val listen: List<String>? = null,
) {
    fun asMap(): Map<String, List<String>> {
        val map = mutableMapOf<String, List<String>>()
        send?.let { map["send"] = it }
        listen?.let { map["listen"] = it }
        return map
    }
}

data class TwitchIncomingJWT(
    val exp: Date,
    val channelId: String, // this is numeric
    val isUnlinked: Boolean,
    val opaqueUserId: String,
    val pubsubPerms: PubsubPerms,
    val role: Role,
    val userId: String? = null, // also numeric
) {
    companion object {
        fun from(payload: Payload): TwitchIncomingJWT {
            val exp = payload.expiresAt
            val channelId = payload.getClaim("channel_id").asString()
            val isUnlinked = payload.getClaim("is_unlinked").asBoolean()
            val opaqueUserId = payload.getClaim("opaque_user_id").asString()
            val pubsubPerms = payload.getClaim("pubsub_perms").`as`(PubsubPerms::class.java)
            val role = Role.fromId(payload.getClaim("role").asString())
            val userId = payload.getClaim("user_id").asString()
            return TwitchIncomingJWT(exp, channelId, isUnlinked, opaqueUserId, pubsubPerms, role, userId)
        }
    }
}

data class TwitchOutgoingJWT(
    val exp: Date,
    val userId: Int,
    val channel_id: String? = null, // this is numeric
    val pubsubPerms: PubsubPerms? = null,
) {

    fun create(): String {
        val builder = JWT.create()
            .withExpiresAt(exp)
            .withClaim("user_id", userId)
            .withClaim("role", "external")

        if (channel_id != null)
            builder.withClaim("channel_id", channel_id)

        if (pubsubPerms != null)
            builder.withClaim("pubsub_perms", pubsubPerms.asMap())

        return builder.sign(algorithm)
    }
}
