import java.time.LocalDateTime

data class Session(
    val id: Int,
    val user: User,
    val token: String,
    val expiration: LocalDateTime,
) {
    init {
        require(id >= 0) { "id must be greater than 0" }
        require(token.isNotBlank()) { "Token must not be blank" }
        require(expiration.isAfter(LocalDateTime.now())) { "Expiration must be in the future" }
    }
}