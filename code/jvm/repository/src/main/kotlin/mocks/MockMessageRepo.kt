package mocks

import Channel
import Message
import MessageRepository
import User
import java.time.LocalDateTime

class MockMessageRepo : MessageRepository {
    /*companion object {
        val ana = User(1, "Ana", "user1@mail.com","token1")
        val joana = User(2, "Joana", "user1@mail.com","token2")
        val joao = User(3, "João", "user1@mail.com","token3")

        val channel1 = Channel(1, "Aulas de DAW", ana, Visibility.PUBLIC)

        val initMsg1 = "Olá, tudo bem?"
        val initMsg2 = "Tudo e convosco?"
        val initMsg3 = "Também!"

        val messages = mutableListOf(
            Message(1, ana.id, channel1.id, initMsg1, LocalDateTime.now()),
            Message(2, joana.id, channel1.id, initMsg2, LocalDateTime.now()),
            Message(3, ana.id, channel1.id, initMsg3, LocalDateTime.now())
        )

    }*/

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

    override fun deleteMessagesByChannel(channelId: Int): List<Message> {
        val messagesToDelete = messages.filter { it.channel.id == channelId }
        messages.removeAll(messagesToDelete)
        return messagesToDelete
    }

    override fun clear() {
        messages.clear()
    }

    override fun findAll(): List<Message> {
        return messages
    }
}
