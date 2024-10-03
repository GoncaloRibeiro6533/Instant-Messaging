import java.time.LocalDateTime

data class Message(
    val id: Int,
    val sender: User,
    val channel: Channel,
    val content: String,
    val timestamp: LocalDateTime

){
    companion object {
        const val MAX_MESSAGE_LENGTH = 1000
    }

    fun init {
        require(id >= 0) { "id must be greater than 0" }
        require(content.isNotBlank()) { "content must not be blank" }
        require(content.length <= MAX_MESSAGE_LENGTH) {
            "content must be less than $MAX_MESSAGE_LENGTH characters" }
    }
}
