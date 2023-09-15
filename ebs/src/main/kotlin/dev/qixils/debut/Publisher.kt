package dev.qixils.debut

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.*
import kotlin.concurrent.thread

val publications: MutableMap<Long, Map<String, *>> = mutableMapOf() // key is the streamer id

private suspend fun publish(channel: String, message: Map<String, *>): HttpResponse {
    val jwt = TwitchOutgoingJWT(
        Date.from(Instant.now().plusSeconds(30)),
        System.getenv("TWITCH_CLIENT_ID"),
        channel,
        PubsubPerms(send = listOf("broadcast"))
    )
    val resp = client.post("https://api.twitch.tv/helix/extensions/pubsub") {
        contentType(ContentType.Application.Json)
        header("Client-Id", System.getenv("TWITCH_CLIENT_ID"))
        header("Authorization", "Bearer ${jwt.create()}")
        setBody(mapOf(
            "target" to listOf("broadcast"),
            "broadcaster_id" to channel,
            "is_global_broadcast" to false,
            "message" to Json.encodeToString(message.toJsonElement()),
        ))
    }
    if (resp.status != HttpStatusCode.OK && resp.status != HttpStatusCode.NoContent) {
        log.error("Failed to publish to Twitch: ${resp.status} ${resp.bodyAsText()}")
    }
    return resp
}

fun startPublisher() {
    thread(start = true, name = "Publisher") { runBlocking {
        while (true) {
            val iter = publications.iterator()
            while (iter.hasNext()) {
                val (channel, message) = iter.next()
                launch { publish(channel.toString(), message) }
                iter.remove()
            }
            delay(1000)
        }
    } }
}