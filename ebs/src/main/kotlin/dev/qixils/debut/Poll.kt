package dev.qixils.debut

import kotlinx.serialization.Serializable

data class Poll(
    val question: String,
    val options: List<String>,
) {
    // map of user ID to option index
    private val votes: MutableMap<String, Int> = mutableMapOf()
    var active = false
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

    fun status(requester: String): PollStatus {
        return PollStatus(
            question,
            options.mapIndexed { index, option ->
                Option(option, votes.values.count { it == index })
            },
            votes.size,
            winner,
            winnerIndex,
            active,
            votes.containsKey(requester),
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
)
