package mocks

import Channel
import ChannelRepository
import Invitation
import Message
import User
import Visibility
import mocks.MockUserRepository.Companion.users
import java.time.LocalDateTime

class MockChannelRepo : ChannelRepository {
    companion object {
        val user = User(1, "Tiago", "token", emptyList(), emptyList())
        val user2 = User(2, "Rui", "token2", emptyList(), emptyList())
        val channels = listOf(Channel(1, "channel1", user, Visibility.PUBLIC, emptyList(), emptyMap()))
        var currentId = 0
    }

    override fun getChannelById(id: Int) = channels.first { it.id == id }

    override fun createChannel(name: String, visibility: Visibility) : Channel {
        return Channel(currentId++,"channel", user, visibility, emptyList(), emptyMap())
    }

    override fun createInvitation(channelId: Int, userId: Int) : Invitation {
        return Invitation(1, user, user2, channels.first { it.id == channelId }, false, LocalDateTime.now())
    }

    override fun getMsgHistory(channelId: Int, limit: Int, skip: Int) = emptyList<Message>()

    override fun getChannelsOfUser(userId: Int) : List<Channel> {
        val user = users.first { it.id == userId }
        return channels.filter { it.users.containsKey(user) }
    }

}