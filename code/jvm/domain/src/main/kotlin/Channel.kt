package pt.isel.talkRooms.domain

data class Channel(
    val id: Int,
    val name: String,
    val creator: User,
    val visibility: Visibility,
    val messages: List<Message>,
    val users: Map<User, Role>
)