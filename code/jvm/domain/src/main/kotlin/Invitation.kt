import java.time.LocalDateTime

data class Invitation(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val channelId: Int,
    val isUsed: Boolean,
    val timestamp: LocalDateTime


){
    init {
        require(id >= 0) { "Id must be greater than 0" }
        require(senderId >= 0) { "SenderId must be greater than 0" }
        require(receiverId >= 0) { "ReceiverId must be greater than 0" }
        require(channelId >= 0) { "ChannelId must be greater than 0" }
        require(senderId != receiverId) { "SenderId must not be equal to ReceiverId" }
        require(!isUsed) { "Invitation is already used" }
    }
}
