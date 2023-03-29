@file:OptIn(KtorExperimentalLocationsAPI::class)

package dev.qixils.debut.plugins

import dev.qixils.debut.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.locations.*
import io.ktor.server.locations.put
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant
import java.util.*

// state

val polls: MutableMap<Long, Poll> = mutableMapOf() // key is the streamer id

fun loadPoll(call: ApplicationCall): Pair<TwitchIncomingJWT, Poll?> {
    val payload = TwitchIncomingJWT.from(call.principal<JWTPrincipal>()!!.payload)
    return payload to polls[payload.channelId.toLong()]
}

suspend fun publish(channel: String, message: Map<String, *>): HttpResponse {
    val jwt = TwitchOutgoingJWT(
        Date.from(Instant.now().plusSeconds(30)),
        System.getenv("TWITCH_CLIENT_ID"),
        channel,
        PubsubPerms(send = listOf("broadcast"))
    )
    return client.post("https://api.twitch.tv/helix/extensions/pubsub") {
        contentType(ContentType.Application.Json)
        header("Client-Id", System.getenv("TWITCH_CLIENT_ID"))
        header("Authorization", "Bearer ${jwt.create()}")
        setBody(mapOf(
            "target" to "broadcast",
            "broadcaster_id" to channel,
            "is_global_broadcast" to false,
            "message" to message,
        ))
    }
}

// routing

fun Application.configureRouting() {
    install(Locations) {
    }

    routing {
        authenticate {
            route("/api") {
                put<PollAPI.Create> {
                    // load payload
                    val principal = call.principal<JWTPrincipal>()!!
                    val payload = TwitchIncomingJWT.from(principal.payload)
                    // ignore if user is not a moderator
                    if (!payload.role.isModerator) {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only moderators can create polls"))
                        return@put
                    }
                    // create & save poll
                    val poll = Poll(it.question, it.options)
                    polls[payload.channelId.toLong()] = poll
                    // publish poll to twitch
                    publish(payload.channelId, mapOf(
                        "type" to "create",
                        "question" to poll.question,
                        "options" to poll.options,
                    ))
                    // respond
                    call.respond(HttpStatusCode.OK)
                }

                put<PollAPI.Vote> {
                    // load poll
                    val (payload, poll) = loadPoll(call)
                    if (poll == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("No poll is active"))
                        return@put
                    }
                    // record vote
                    try {
                        poll.vote(payload.opaqueUserId, it.option)
                    } catch (ex: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(ex))
                        return@put
                    }
                    // respond
                    call.respond(HttpStatusCode.OK)
                }

                put<PollAPI.Close> {
                    // load poll
                    val (payload, poll) = loadPoll(call)
                    if (poll == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("No poll is active"))
                        return@put
                    }
                    // ignore if user is not a moderator
                    if (!payload.role.isModerator) {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only moderators can close polls"))
                        return@put
                    }
                    // close poll
                    try {
                        poll.close()
                    } catch (ex: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(ex))
                        return@put
                    }
                    // publish closure to twitch
                    publish(payload.channelId, mapOf(
                        "type" to "close",
                    ))
                    // respond
                    call.respond(HttpStatusCode.OK, poll.status(payload.opaqueUserId))
                }

                get<PollAPI.Status> {
                    // load poll
                    val (payload, poll) = loadPoll(call)
                    if (poll == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("No poll is active"))
                        return@get
                    }
                    // respond
                    call.respond(HttpStatusCode.OK, poll.status(payload.opaqueUserId))
                }
            }
        }
    }
}

@Location("/poll")
class PollAPI {
    @Location("/create")
    data class Create(val question: String, val options: List<String>)

    @Location("/vote")
    data class Vote(val option: Int)

    @Location("/close")
    class Close

    @Location("/status")
    class Status
}
