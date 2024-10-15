import java.time.LocalDateTime

class RegisterInvitation(
    id: Int,
    sender: User,
    val email: String,
    val channel: Channel,
    val role: Role,
    isUsed: Boolean = false,
    timestamp: LocalDateTime,
) : Invitation(id, sender, isUsed, timestamp) {
    init {
        require(id >= 0) { "id must be greater than 0" }
        require(email.isNotBlank()) { "Email must not be blank" }
        require(sender.email != email) { "Sender and receiver email must be different" }
        require(timestamp <= LocalDateTime.now()) { "Invalid timestamp" }
    }

    private fun copy() = RegisterInvitation(id, sender, email, channel, role, true, timestamp)

    override fun markAsUsed() = copy()
}
