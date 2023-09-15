package dev.qixils.debut

import com.auth0.jwt.algorithms.Algorithm
import dev.qixils.debut.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

fun main() {
    embeddedServer(Netty, port = 33288, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
    startPublisher()
}

//val secret = this@configureSecurity.environment.config.property("jwt.secret").getString()
val algorithm: Algorithm = Algorithm.HMAC256(Base64.getDecoder().decode(System.getenv("JWT_SECRET")))

val log: Logger = LoggerFactory.getLogger("Application")