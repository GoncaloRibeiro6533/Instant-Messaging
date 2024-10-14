package models.user

data class UserOutput(
    val token: String,
    val id: Int,
    val username: String,
)
