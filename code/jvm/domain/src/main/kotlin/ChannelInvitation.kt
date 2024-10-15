import java.time.LocalDateTime

class ChannelInvitation(
    id: Int,
    sender: User,
    val receiver: User,
    val channel: Channel,
    val role: Role,
    isUsed: Boolean = false,
    timestamp: LocalDateTime,
) : Invitation(id, sender, isUsed, timestamp) {
    init {
        require(id >= 0) { "id must be greater than 0" }
        require(role in Role.entries.toTypedArray()) { "Invalid role" }
        require(sender != receiver) { "Sender and receiver must be different" }
        require(timestamp <= LocalDateTime.now()) { "Invalid timestamp" }
    }

    private fun copy() = ChannelInvitation(id, sender, receiver, channel, role, true, timestamp)

    override fun markAsUsed() = copy()
}
