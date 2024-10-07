import java.time.LocalDateTime

sealed class Invitation(
    val id: Int,
    val sender: User,
    val isUsed: Boolean,
    val timestamp: LocalDateTime


) {
    abstract fun markAsUsed(): Invitation
}

class ChannelInvitation(
    id: Int,
    sender: User,
    val receiver: User,
    val channel: Channel,
    val role: Role,
    isUsed: Boolean = false,
    timestamp: LocalDateTime
) : Invitation(id, sender, isUsed, timestamp){
    init {
        require(id >= 0) { "id must be greater than 0" }
        require(role in Role.values()) { "Invalid role" }
        require(sender != receiver) { "Sender and receiver must be different" }
        require(!isUsed) { "Invalid isUsed" }
        require(timestamp <= LocalDateTime.now()) { "Invalid timestamp" }
    }


    private fun copy() = ChannelInvitation(id, sender, receiver, channel, role, true, timestamp)
    override fun markAsUsed() = copy()
}

class RegisterInvitation(
    id: Int,
    sender: User,
    val email: String,
    val channel: Channel? = null,
    val role: Role? = null,
    isUsed: Boolean = false,
    timestamp: LocalDateTime
) : Invitation(id, sender, isUsed, timestamp){
    init {
        require(id >= 0) { "id must be greater than 0" }
        require(email.isNotBlank()) { "Email must not be blank" }
        require(sender.email != email) { "Sender and receiver email must be different" }
        require(!isUsed) { "Invalid isUsed" }
        require(timestamp <= LocalDateTime.now()) { "Invalid timestamp" }
    }

    private fun copy() =
        RegisterInvitation(id, sender, email, channel, role, true, timestamp)
    override fun markAsUsed() = copy()
}
