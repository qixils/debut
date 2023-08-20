package dev.qixils.debut.plugins

import com.auth0.jwt.JWT
import dev.qixils.debut.algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {

    authentication {
        jwt {
            verifier(JWT.require(algorithm).build())
            validate { credential ->
                if (!credential.payload.getClaim("channel_id").asString().isNullOrEmpty())
                    JWTPrincipal(credential.payload)
                else
                    null
            }
        }
    }

}
