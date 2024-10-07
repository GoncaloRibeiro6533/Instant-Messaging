package mocks

import Channel
import ChannelRepository
import Message
import Role
import User
import Visibility

class MockChannelRepository : ChannelRepository {

        val users = mutableListOf(
            User(1, "Tiago", "user1@mail.com","token"),
            User(2, "João", "user1@mail.com", "token1"))
        val channels = listOf(Channel(1, "channel1", users[0].id, Visibility.PUBLIC, emptyList(), mapOf(Pair(users[0],Role.READ_WRITE), Pair(users[1],Role.READ_ONLY))),
            )


        var currentId = 0


    override fun findById(id: Int) = channels.firstOrNull { it.id == id }

    override fun getChannelByName(name: String) = channels.firstOrNull { it.name == name }

    override fun createChannel(name: String, creatorId: Int, visibility: Visibility) : Channel {
        return Channel(currentId++,name, creatorId, visibility, emptyList(), emptyMap())
    }

    override fun getMsgHistory(channelId: Int, limit: Int, skip: Int) = emptyList<Message>()

    override fun getChannelsOfUser(userId: Int): List<Channel>? {
        val userChannels = channels.filter { channel -> channel.users.keys.any { user -> user.id == userId } }
        return userChannels.ifEmpty { null } //todo se o usuário não tiver canais, retorna null? mas depois no services ta a retornar a execessao errada
    }

    override fun getChannelMembers(channelId: Int): List<User>? = channels.firstOrNull { it.id == channelId }?.users?.keys?.toList()

}