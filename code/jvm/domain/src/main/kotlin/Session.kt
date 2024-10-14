import java.time.LocalDateTime

data class Session(
    val user: User,
    val token: String,
    val expiration: LocalDateTime,
) {
    init {
        require(token.isNotBlank()) { "Token must not be blank" }
        require(expiration.isAfter(LocalDateTime.now())) { "Expiration must be in the future" }
    }
}