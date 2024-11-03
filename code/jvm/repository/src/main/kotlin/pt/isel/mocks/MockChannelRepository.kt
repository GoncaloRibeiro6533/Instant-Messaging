package pt.isel.mocks

import pt.isel.Channel
import pt.isel.ChannelRepository
import pt.isel.Role
import pt.isel.User
import pt.isel.Visibility

class MockChannelRepository : ChannelRepository {
    private data class UserRole(
        val userId: Int,
        val role: Role,
    )

    private val channels = mutableListOf<Channel>()
    private val usersInChannel = mutableSetOf<Pair<Int, UserRole>>() // <ChannelId,Pair<UserId,Role>>
    private var currentId = 0

    override fun findById(id: Int) = channels.firstOrNull { it.id == id }

    override fun getChannelByName(
        name: String,
        limit: Int,
        skip: Int,
    ): List<Channel> =
        channels.filter { it.name.trim().uppercase().contains(name.uppercase()) }
            .drop(skip)
            .take(limit)

    override fun createChannel(
        name: String,
        creator: User,
        visibility: Visibility,
    ): Channel {
        val channel = Channel(currentId++, name, creator, visibility)
        channels.add(channel)
        usersInChannel.add(channel.id to UserRole(creator.id, Role.READ_WRITE))
        return channel
    }

    override fun getChannelsOfUser(user: User): List<Channel> {
        // Filtrar todos os channelIds nos quais o userId está presente
        val userChannelIds =
            usersInChannel
                .filter { it.second.userId == user.id } // it.second.first é o userId
                .map { it.first } // it.first é o channelId

        // Procurar os canais correspondentes a esses channelIds
        val userChannels = channels.filter { it.id in userChannelIds }

        // Retornar a lista de canais ou null se o user não tiver canais
        return userChannels.ifEmpty { emptyList() }
    }

    override fun getChannelMembers(channel: Channel): List<Int> {
        // Filtrar os userIds dos membros do canal usando usersInChannel
        val userIdsInChannel =
            usersInChannel
                .filter { it.first == channel.id } // Filtra os pares que têm o mesmo channelId
                .map { it.second.userId } // Mapeia para userId (it.second.first)

        // Retornar a lista de usuários ou null se não houver membros
        return userIdsInChannel.ifEmpty { emptyList() }
    }

    override fun addUserToChannel(
        user: User,
        channel: Channel,
        role: Role,
    ): Channel {
        usersInChannel.add(channel.id to UserRole(user.id, role))
        return channel
    }

    override fun updateChannelName(
        channel: Channel,
        name: String,
    ): Channel {
        val updatedChannel = channel.copy(name = name)
        channels.replaceAll { if (it.id == channel.id) updatedChannel else it }
        return updatedChannel
    }

    override fun leaveChannel(
        user: User,
        channel: Channel,
    ): Channel {
        usersInChannel.remove(channel.id to UserRole(user.id, Role.READ_WRITE))
        return channel
    }

    override fun clear() {
        channels.clear()
        usersInChannel.clear()
        currentId = 0
    }
}
