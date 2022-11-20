package dev.qixils.debut

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

    fun status(requester: String): Map<String, *> {
        return mapOf(
            "question" to question,
            "options" to options,
            "tallies" to votes.values.groupingBy { it }.eachCount(),
            "totalVotes" to votes.size,
            "winner" to winner,
            "active" to active,
            "hasVoted" to votes.containsKey(requester),
        )
    }
}
