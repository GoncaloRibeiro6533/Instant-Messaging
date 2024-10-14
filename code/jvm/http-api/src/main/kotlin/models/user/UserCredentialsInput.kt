package models.user

data class UserCredentialsInput(
    val username: String,
    val email: String,
    val password: String,
)
