data class User(
    val id: Int,
    val username: String,
    val token: String,
    val channels: List<Channel> = emptyList(),
    val invitations: List<Invitation> = emptyList(),
    val unreadMessages: List<Message> = emptyList()
){

    companion object {
       const val MAX_USERNAME_LENGTH = 50
    }

    init {
        require(id >= 0) { "id must be greater than 0" }
        require(username.isNotBlank()) { "username must not be blank" }
        require(username.length <= MAX_USERNAME_LENGTH) {
            "username must be less than $MAX_USERNAME_LENGTH characters" }
        require(token.isNotBlank()) { "token must not be blank" }
    }
}