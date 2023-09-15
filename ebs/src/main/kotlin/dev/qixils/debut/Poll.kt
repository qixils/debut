package dev.qixils.debut

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

data class Poll(
    val question: String,
    val options: List<String>,
) {
    // map of user ID to option index
    private val votes: MutableMap<String, Int> = mutableMapOf()
    var active = true
    var winnerIndex: Int? = null
    val winner: String?
        get() = winnerIndex?.let { options[it] }

    fun vote(userId: String, option: Int) {
        if (option !in options.indices)
            throw IllegalStateException("Invalid option index")
        if (!active)
            throw IllegalStateException("Poll is closed")
        votes[userId] = option
    }

    fun close() {
        if (!active)
            throw IllegalStateException("Poll is already closed")
        winnerIndex = votes.values.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        active = false
    }

    fun status(requester: String? = null): PollStatus {
        return PollStatus(
            question=question,
            options=options.mapIndexed { index, option ->
                Option(option, votes.values.count { it == index })
            },
            totalVotes=votes.size,
            winner=winner,
            winnerIndex=winnerIndex,
            active=active,
            hasVoted=requester != null && requester in votes,
        )
    }
}

@Serializable
data class Option(
    val value: String,
    val votes: Int,
)

@Serializable
data class PollStatus(
    val question: String,
    val options: List<Option>,
    val totalVotes: Int,
    val winner: String?,
    val winnerIndex: Int?,
    val active: Boolean,
    val hasVoted: Boolean,
) : JsonSerializable {
    override fun toJson(): JsonElement {
        return mapOf(
            "question" to question,
            "options" to options,
            "totalVotes" to totalVotes,
            "winner" to winner,
            "winnerIndex" to winnerIndex,
            "active" to active,
            "hasVoted" to hasVoted,
        ).toJsonElement()
    }
}
