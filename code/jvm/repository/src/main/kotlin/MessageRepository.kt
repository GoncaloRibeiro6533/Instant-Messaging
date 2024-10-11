interface MessageRepository {
    fun findById(id: Int): Message?

    fun sendMessage(
        sender: User,
        channel: Channel,
        text: String,
    ): Message

    fun getMsgHistory(
        channel: Channel,
        limit: Int,
        skip: Int,
    ): List<Message>

    // fun getMsgHistory(channelId: Int, limit: Int, skip: Int): List<Message>
}
