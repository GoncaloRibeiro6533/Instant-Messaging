package mocks

import Channel
import Message
import MessageRepository
import User
import Visibility
import java.time.LocalDateTime

class MockMessageRepo : MessageRepository {
    companion object {
        val ana = User(1, "Ana", "token1", emptyList(), emptyList())
        val joana = User(2, "Joana", "token2", emptyList(), emptyList())
        val joao = User(3, "João", "token3", emptyList(), emptyList())

        val channel1 = Channel(1, "Aulas de DAW", ana, Visibility.PUBLIC, emptyList(), emptyMap())

        val initMsg1 = "Olá, tudo bem?"
        val initMsg2 = "Tudo e convosco?"
        val initMsg3 = "Também!"

        val messages = mutableListOf(
            Message(1, ana.id, channel1.id, initMsg1, LocalDateTime.now()),
            Message(2, joana.id, channel1.id, initMsg2, LocalDateTime.now()),
            Message(3, ana.id, channel1.id, initMsg3, LocalDateTime.now())
        )

    }

    override fun findMessageById(id: Int): Message? {
        return messages.firstOrNull { it.id == id }
    }

    override fun sendMessage(senderId: Int, channelId: Int, text: String): Message {
        val message = Message(messages.size + 1, senderId, channelId, text, LocalDateTime.now())
        messages.add(message)
        return message
    }

}