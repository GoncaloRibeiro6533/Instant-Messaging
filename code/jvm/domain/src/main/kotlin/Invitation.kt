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
        require(id >= 0) { "id must be greater than 0" }
        require(!isUsed) { "invitation must be new" }

    }
}
