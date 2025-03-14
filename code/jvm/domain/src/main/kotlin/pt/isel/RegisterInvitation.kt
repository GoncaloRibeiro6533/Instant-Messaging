package pt.isel

import java.time.LocalDateTime

class RegisterInvitation(
    id: Int,
    sender: User,
    val email: String,
    val channel: Channel,
    val role: Role,
    isUsed: Boolean = false,
    timestamp: LocalDateTime,
    val code: String,
) : Invitation(id, sender, isUsed, timestamp) {
    init {
        require(id >= 0) { "id must be greater than 0" }
        require(email.isNotBlank()) { "Email must not be blank" }
        require(sender.email != email) { "Sender and receiver email must be different" }
        require(timestamp <= LocalDateTime.now()) { "Invalid timestamp" }
        require(code.isNotBlank()) { "Code must not be blank" }
    }

    fun copy() = RegisterInvitation(id, sender, email, channel, role, true, timestamp, code)

    override fun markAsUsed() = copy()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RegisterInvitation) return false

        if (id != other.id) return false
        if (sender != other.sender) return false
        if (email != other.email) return false
        if (channel != other.channel) return false
        if (role != other.role) return false
        if (isUsed != other.isUsed) return false
        if (timestamp != other.timestamp) return false
        if (code != other.code) return false

        return true
    }
}
