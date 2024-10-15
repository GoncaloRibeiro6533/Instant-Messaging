import org.jdbi.v3.core.Handle

class JdbiChannelRepository(
    private val handle: Handle,
) : ChannelRepository {
    override fun findById(id: Int): Channel? {
        TODO("Not yet implemented")
    }

    override fun getChannelByName(name: String): Channel? {
        TODO("Not yet implemented")
    }

    override fun createChannel(
        name: String,
        creator: User,
        visibility: Visibility,
    ): Channel {
        TODO("Not yet implemented")
    }

    override fun getChannelsOfUser(user: User): List<Channel> {
        TODO("Not yet implemented")
    }

    override fun getChannelMembers(channel: Channel): List<Int> {
        TODO("Not yet implemented")
    }

    override fun addUserToChannel(
        user: User,
        channel: Channel,
        role: Role,
    ): Channel {
        TODO("Not yet implemented")
    }

    override fun updateChannelName(
        channel: Channel,
        name: String,
    ): Channel {
        TODO("Not yet implemented")
    }

    override fun leaveChannel(
        user: User,
        channel: Channel,
    ): Channel {
        TODO("Not yet implemented")
    }
}
