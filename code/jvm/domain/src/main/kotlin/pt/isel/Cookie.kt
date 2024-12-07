package pt.isel

import kotlinx.datetime.Instant

data class Cookie(
    val user: User,
    val token: String,
    val expireDate: Instant,
) {
    init {
        require(token.isNotEmpty())
    }
}
