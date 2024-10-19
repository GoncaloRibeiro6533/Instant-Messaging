package pt.isel

interface MessageRepository {
    fun findById(id: Int): Message?

    fun createMessage(
        sender: User,
        channel: Channel,
        text: String,
    ): Message

    fun findByChannel(
        channel: Channel,
        limit: Int,
        skip: Int,
    ): List<Message>

    fun deleteMessageById(id: Int): Message?

    fun deleteMessagesByChannel(channelId: Int): Boolean

    fun findAll(): List<Message>

    fun clear(): Unit
    // fun getMsgHistory(channelId: Int, limit: Int, skip: Int): List<Message>
}
