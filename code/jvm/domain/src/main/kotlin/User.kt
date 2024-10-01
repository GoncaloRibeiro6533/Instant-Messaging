data class User(
    val id: Int,
    val username: String,
    val token: String,
    val channels: List<Channel>,
    val invitations: List<Invitation>
)