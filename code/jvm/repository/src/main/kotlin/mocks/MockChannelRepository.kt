package mocks

import Channel
import ChannelRepository
import Role
import User
import Visibility

typealias ChannelUserInfo = Pair<Int, Role>

class MockChannelRepository : ChannelRepository {

    private val channels = mutableListOf<Channel>()
    private val usersInChannel = mutableListOf<Pair<Int, ChannelUserInfo>>() //<ChannelId,Pair<UserId,Role>>
    private var currentId = 0

    override fun findById(id: Int) = channels.firstOrNull { it.id == id }

    override fun getChannelByName(name: String) = channels.firstOrNull { it.name == name }

    override fun createChannel(name: String, creator: User, visibility: Visibility) : Channel {
        val channel = Channel(currentId++, name, creator, visibility)
        channels.add(channel)
        addUserToChannel(creator.id, channel.id, Role.READ_WRITE)
        return channel
    }

    override fun getChannelsOfUser(userId: Int): List<Channel> {
        // Filtrar todos os channelIds nos quais o userId está presente
        val userChannelIds = usersInChannel
            .filter { it.second.first == userId }  // it.second.first é o userId
            .map { it.first }  // it.first é o channelId

        // Procurar os canais correspondentes a esses channelIds
        val userChannels = channels.filter { it.id in userChannelIds }

        // Retornar a lista de canais ou null se o user não tiver canais
        return userChannels.ifEmpty { emptyList() }
    }

    override fun getChannelMembers(channelId: Int): List<Int> {

        // Filtrar os userIds dos membros do canal usando usersInChannel
        val userIdsInChannel = usersInChannel
            .filter { it.first == channelId }  // Filtra os pares que têm o mesmo channelId
            .map { it.second.first }  // Mapeia para userId (it.second.first)

        // Retornar a lista de usuários ou null se não houver membros
        return userIdsInChannel.ifEmpty { emptyList() }
    }

    override fun addUserToChannel(userId: Int, channelId: Int, role: Role): Channel? {
        if (usersInChannel.any { it.first == channelId && it.second.first == userId }) return null
        usersInChannel.add(channelId to (userId to role))
        return findById(channelId)
    }
}