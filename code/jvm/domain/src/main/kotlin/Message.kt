import java.time.LocalDateTime

data class Message(
    val id: Int,
    val sender: User,
    val channel: Channel,
    val content: String,
    val timestamp: LocalDateTime
)
