import java.time.LocalDateTime

data class Invitation(
    val id: Int,
    val sender: User,
    val receiver: User,
    val channel: Channel,
    val isUsed: Boolean,
    val timestamp: LocalDateTime


){
    init {
        require(id >= 0) { "id must be greater than 0" }
        require(!isUsed) { "invitation must be new" }

    }
}
