package pt.isel.mocks

import pt.isel.Channel
import pt.isel.ChannelRepository
import pt.isel.Role
import pt.isel.User
import pt.isel.Visibility

class MockChannelRepository : ChannelRepository {
    private data class UserRole(
        val user: User,
        val role: Role,
    )

    private val channels = mutableListOf<Channel>()
    private val usersInChannel = mutableSetOf<Pair<Channel, UserRole>>() // <ChannelId,Pair<UserId,Role>>
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
        return channel
    }

    override fun getChannelsOfUser(user: User): List<Channel> {
        val userChannels: List<Channel> =
            usersInChannel
                .filter { it.second.user.id == user.id }
                .map { it.first }
        return userChannels
    }

    override fun getChannelMembers(channel: Channel): Map<User, Role> {
        val users =
            usersInChannel
                .filter { it.first.id == channel.id }.map { it.second }
        val usersMap = users.associate { it.user to it.role }
        return usersMap
    }

    override fun addUserToChannel(
        user: User,
        channel: Channel,
        role: Role,
    ): Channel {
        usersInChannel.add(channel to UserRole(user, role))
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
        val userToLeaveCh: Pair<Channel, UserRole> =
            usersInChannel.first { it.first.id == channel.id && it.second.user.id == user.id }
        usersInChannel.remove(userToLeaveCh)
        return channel
    }

    override fun clear() {
        channels.clear()
        usersInChannel.clear()
        currentId = 0
    }
}
