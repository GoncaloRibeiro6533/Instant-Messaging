package pt.isel

import java.time.LocalDateTime

data class Message(
    val id: Int,
    val sender: User,
    val channel: Channel,
    val content: String,
    val timestamp: LocalDateTime,
) {
    companion object {
        const val MAX_MESSAGE_LENGTH = 1000
    }

    init {
        require(id >= 0) { "Id must be greater than 0" }
        require(content.isNotBlank()) { "A message can't be blank" }
        require(content.length <= MAX_MESSAGE_LENGTH) {
            "Content must be less than $MAX_MESSAGE_LENGTH characters"
        }
        require(timestamp <= LocalDateTime.now()) { "Invalid timestamp" }
    }
}
