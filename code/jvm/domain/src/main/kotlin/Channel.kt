data class Channel(
    val id: Int,
    val name: String,
    val creatorId: Int,
    val visibility: Visibility,
    val messages: List<Message>,
    val users: Map<User, Role>
){
    init {
        require(id >= 0) { "id must be greater than 0" }
        require(name.isNotBlank()) { "Channel name must not be blank" }
        require(creatorId >= 0) { "Creator id must be greater than 0" }
        require(visibility in Visibility.entries.toTypedArray()) { "Invalid visibility" }
    }
}
