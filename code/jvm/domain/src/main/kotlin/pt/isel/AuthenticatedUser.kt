package pt.isel

data class AuthenticatedUser(
    val user: User,
    val token: String,
) {
    init {
        require(token.isNotEmpty())
    }
}
