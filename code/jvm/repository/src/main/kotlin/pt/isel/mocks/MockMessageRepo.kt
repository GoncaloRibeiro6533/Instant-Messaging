package pt.isel.mocks

import pt.isel.Channel
import pt.isel.Message
import pt.isel.MessageRepository
import pt.isel.User
import java.time.LocalDateTime

class MockMessageRepo : MessageRepository {
    private val messages = mutableListOf<Message>()

    override fun findById(id: Int): Message? {
        if (messages.isEmpty()) return null
        return messages.firstOrNull { it.id == id }
    }

    override fun createMessage(
        sender: User,
        channel: Channel,
        text: String,
    ): Message {
        val message = Message(messages.size + 1, sender, channel, text, LocalDateTime.now())
        messages.add(message)
        return message
    }

    override fun findByChannel(
        channel: Channel,
        limit: Int,
        skip: Int,
    ): List<Message> {
        return messages.filter { it.channel.id == channel.id }
            .drop(skip)
            .take(limit)
    }

    override fun deleteMessageById(id: Int): Message {
        val message = messages.first { it.id == id }
        messages.remove(message)
        return message
    }

    override fun deleteMessagesByChannel(channelId: Int): Boolean {
        val messagesToDelete = messages.filter { it.channel.id == channelId }
        messages.removeAll(messagesToDelete)
        return true
    }

    override fun clear() {
        messages.clear()
    }

    override fun findAll(): List<Message> {
        return messages
    }
}
