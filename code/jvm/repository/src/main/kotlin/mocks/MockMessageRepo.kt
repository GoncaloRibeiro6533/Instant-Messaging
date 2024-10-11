package mocks

import Channel
import Message
import MessageRepository
import User
import Visibility
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
        if(messages.isEmpty()) return null
        return messages.firstOrNull { it.id == id }
    }

    override fun sendMessage(senderId: Int, channelId: Int, text: String): Message {
        val message = Message(messages.size + 1, senderId, channelId, text, LocalDateTime.now())
        messages.add(message)
        return message
    }

    override fun getMsgHistory(channelId: Int, limit: Int, skip: Int): List<Message> {
        return messages.filter { it.channelId == channelId }
            .drop(skip)
            .take(limit)
    }

}