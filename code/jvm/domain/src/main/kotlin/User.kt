data class User(
    val id: Int,
    val username: String,
    val token: String,
){

    init {
        require(id >= 0) { "Id cannot be negative" }
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(username.length <= MAX_USERNAME_LENGTH) {
            "Username cannot be longer than $MAX_USERNAME_LENGTH characters" }
        require(token.isNotBlank()) { "Token cannot be blank" }
    }
    companion object {
       const val MAX_USERNAME_LENGTH = 50
    }
}