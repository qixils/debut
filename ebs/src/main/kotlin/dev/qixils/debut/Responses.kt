package dev.qixils.debut

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String) {
    constructor(ex: Throwable) : this(ex.message ?: ex.toString())
}
