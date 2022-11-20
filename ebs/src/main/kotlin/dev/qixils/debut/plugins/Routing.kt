@file:OptIn(KtorExperimentalLocationsAPI::class)

package dev.qixils.debut.plugins

import dev.qixils.debut.Poll
import dev.qixils.debut.TwitchIncomingJWT
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.locations.*
import io.ktor.server.locations.put
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// state

val polls: MutableMap<Long, Poll> = mutableMapOf() // key is the streamer id

fun loadPoll(call: ApplicationCall): Pair<TwitchIncomingJWT, Poll?> {
    val payload = TwitchIncomingJWT.from(call.principal<JWTPrincipal>()!!.payload)
    return payload to polls[payload.channelId.toLong()]
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
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only moderators can create polls"))
                        return@put
                    }
                    // create & save poll
                    val poll = Poll(it.question, it.options)
                    polls[payload.channelId.toLong()] = poll
                    // publish poll to twitch
                    // TODO
                    // begin task to periodically publish status to moderator view
                    // TODO
                    // respond
                    call.respond(HttpStatusCode.OK)
                }

                put<PollAPI.Vote> {
                    // load poll
                    val (payload, poll) = loadPoll(call)
                    if (poll == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "No poll is active"))
                        return@put
                    }
                    // record vote
                    try {
                        poll.vote(payload.opaqueUserId, it.option)
                    } catch (ex: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to ex.message))
                        return@put
                    }
                    // respond
                    call.respond(HttpStatusCode.OK)
                }

                put<PollAPI.Close> {
                    // load poll
                    val (payload, poll) = loadPoll(call)
                    if (poll == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "No poll is active"))
                        return@put
                    }
                    // ignore if user is not a moderator
                    if (!payload.role.isModerator) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only moderators can close polls"))
                        return@put
                    }
                    // close poll
                    try {
                        poll.close()
                    } catch (ex: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to ex.message))
                        return@put
                    }
                    // publish closure to twitch
                    // TODO
                    // respond
                    call.respond(HttpStatusCode.OK, poll.status(payload.opaqueUserId))
                }

                put<PollAPI.Status> {
                    // load poll
                    val (payload, poll) = loadPoll(call)
                    if (poll == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "No poll is active"))
                        return@put
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
