package mocks

import Channel
import ChannelRepository
import Message
import User
import Visibility

class MockChannelRepository : ChannelRepository {
    companion object {
        val users1 = mutableListOf(
            User(1, "Tiago", "token"))
        val channels = listOf(Channel(1, "channel1", users1[0].id, Visibility.PUBLIC, emptyList(), emptyMap()))
        val usersWithChannel = mutableListOf(
            User(1, "Rui", "token"),
        )
        var currentId = 0
    }

    override fun getChannelById(id: Int) = channels.firstOrNull { it.id == id }

    override fun getChannelByName(name: String) = channels.firstOrNull { it.name == name }

    override fun createChannel(name: String, creatorId: Int, visibility: Visibility) : Channel {
        return Channel(currentId++,name, creatorId, visibility, emptyList(), emptyMap())
    }

    override fun getMsgHistory(channelId: Int, limit: Int, skip: Int) = emptyList<Message>()

   override fun getChannelsOfUser(userId: Int) : List<Channel> {
        TODO("Not yet implemented")
    }

}