import kotlinx.datetime.Instant

data class Session(
    val token: TokenValidationInfo,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
) {
    init {
        // require(token.isNotBlank()) { "Token must not be blank" }
        // require(expiration.isAfter(LocalDateTime.now())) { "Expiration must be in the future" }
    }

    // fun expired(): Boolean = expiration.isBefore(LocalDateTime.now())
}
