package pt.isel

import kotlinx.datetime.Instant

data class Token(
    val token: TokenValidationInfo,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
) {
    init {
        require(userId >= 0) { "Invalid user id" }
        require(token.validationInfo.isNotEmpty()) { "Invalid token validation info" }
    }
}
