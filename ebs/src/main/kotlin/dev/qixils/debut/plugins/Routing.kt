
package dev.qixils.debut.plugins

import dev.qixils.debut.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant
import java.util.*
import kotlin.collections.set

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
    install(Resources)

    routing {
        authenticate {
            route("/api/poll") {
                post<Create> {
                    // load payload
                    val principal = call.principal<JWTPrincipal>()!!
                    val payload = TwitchIncomingJWT.from(principal.payload)
                    // ignore if user is not a moderator
                    if (!payload.role.isModerator) {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only moderators can create polls"))
                        return@post
                    }
                    // create poll by reading question and options from lines of body
                    val lines = call.receiveText().trim().lines()
                    if (lines.size < 2) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Poll must have a question and at least one option"))
                        return@post
                    }
                    val poll = Poll(lines[0], lines.drop(1))
                    polls[payload.channelId.toLong()] = poll
                    // publish poll to twitch
                    publish(payload.channelId, mapOf(
                        "type" to "create",
                        "status" to poll.status(payload.opaqueUserId),
                    ))
                    // respond
                    call.respond(HttpStatusCode.OK)
                }

                post<Vote> {
                    // load poll
                    val (payload, poll) = loadPoll(call)
                    if (poll == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("No poll is active"))
                        return@post
                    }
                    // record vote
                    try {
                        poll.vote(payload.opaqueUserId, it.option)
                    } catch (ex: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(ex))
                        return@post
                    }
                    // publish vote to twitch
                    publish(payload.channelId, mapOf(
                        "type" to "vote",
                        "status" to poll.status(payload.opaqueUserId),
                    ))
                    // respond
                    call.respond(HttpStatusCode.OK)
                }

                post<Close> {
                    // load poll
                    val (payload, poll) = loadPoll(call)
                    if (poll == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("No poll is active"))
                        return@post
                    }
                    // ignore if user is not a moderator
                    if (!payload.role.isModerator) {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only moderators can close polls"))
                        return@post
                    }
                    // close poll
                    try {
                        poll.close()
                    } catch (ex: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(ex))
                        return@post
                    }
                    // publish closure to twitch
                    publish(payload.channelId, mapOf(
                        "type" to "close",
                        "status" to poll.status(payload.opaqueUserId),
                    ))
                    // respond
                    call.respond(HttpStatusCode.OK, poll.status(payload.opaqueUserId))
                }

                get<Status> {
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

@Resource("/create")
class Create

@Resource("/vote")
data class Vote(val option: Int)

@Resource("/close")
class Close

@Resource("/status")
class Status
