data class Channel(
    val id: Int,
    val name: String,
    val creatorId: Int,
    val visibility: Visibility,
    val messages: List<Message>,
    val users: Map<User, Role>
)