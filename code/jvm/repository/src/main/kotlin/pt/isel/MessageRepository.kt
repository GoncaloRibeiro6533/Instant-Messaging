package pt.isel

import kotlinx.datetime.Instant

interface MessageRepository {
    fun findById(id: Int): Message?

    fun createMessage(
        sender: User,
        channel: Channel,
        text: String,
        creationTime: Instant,
    ): Message

    fun findByChannel(
        channel: Channel,
        limit: Int,
        skip: Int,
    ): List<Message>

    fun deleteMessageById(message: Message): Message

    fun deleteMessagesByChannel(channelId: Int): Boolean

    fun findAll(): List<Message>

    fun clear(): Unit
    // fun getMsgHistory(channelId: Int, limit: Int, skip: Int): List<Message>
}
