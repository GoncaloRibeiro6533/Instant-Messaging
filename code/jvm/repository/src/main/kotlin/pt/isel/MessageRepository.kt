package pt.isel

import java.time.LocalDateTime

interface MessageRepository {
    fun createMessage(
        sender: User,
        channel: Channel,
        text: String,
        creationTime: LocalDateTime,
    ): Message

    fun findById(id: Int): Message?

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
