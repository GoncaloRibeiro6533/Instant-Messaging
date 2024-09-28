package pt.isel.talkRooms.domain

data class Channel(
    val id: Int,
    val name: String,
    val creator: User,
    val visibility: Visibility,
    val admins: User, //TODO one or more??
    val numberOfUsers: Int,
    val messages: List<Message>,
    val users: Map<User, Role>
)