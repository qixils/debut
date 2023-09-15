
package dev.qixils.debut.plugins

import dev.qixils.debut.ErrorResponse
import dev.qixils.debut.Poll
import dev.qixils.debut.TwitchIncomingJWT
import dev.qixils.debut.publications
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
import kotlin.collections.set

// state

val polls: MutableMap<Long, Poll> = mutableMapOf() // key is the streamer id

fun loadPoll(call: ApplicationCall): Pair<TwitchIncomingJWT, Poll?> {
    val payload = TwitchIncomingJWT.from(call.principal<JWTPrincipal>()!!.payload)
    return payload to polls[payload.channelId.toLong()]
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
                    publications[payload.channelId.toLong()] = mapOf(
                        "type" to "create",
                        "status" to poll.status(),
                    )
                    // respond
                    call.respond(HttpStatusCode.OK, poll.status(payload.opaqueUserId))
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
                    publications[payload.channelId.toLong()] = mapOf(
                        "type" to "vote",
                        "status" to poll.status(),
                    )
                    // respond
                    call.respond(HttpStatusCode.OK, poll.status(payload.opaqueUserId))
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
                    publications[payload.channelId.toLong()] = mapOf(
                        "type" to "close",
                        "status" to poll.status(),
                    )
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
