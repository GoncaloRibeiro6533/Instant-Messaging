package pt.isel

data class User(
    val id: Int,
    val username: String,
    val email: String,
) {
    init {
        require(id >= 0) { "Id cannot be negative" }
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(username.length <= MAX_USERNAME_LENGTH) {
            "Username cannot be longer than $MAX_USERNAME_LENGTH characters"
        }
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-z.-]+\\.[a-z]{2,4}$"
        require(email.matches(emailRegex.toRegex())) {
            "Invalid Email: $email"
        }
    }

    companion object {
        const val MAX_SESSIONS = 5
        const val MAX_USERNAME_LENGTH = 20
        const val MIN_USERNAME_LENGTH = 3
    }
}
