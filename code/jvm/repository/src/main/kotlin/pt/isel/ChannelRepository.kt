package pt.isel

interface ChannelRepository {
    fun findById(id: Int): Channel?

    fun getChannelByName(
        name: String,
        limit: Int,
        skip: Int,
    ): List<Channel>

    fun createChannel(
        name: String,
        creator: User,
        visibility: Visibility,
    ): Channel

    fun getChannelsOfUser(user: User): Map<Channel, Role>

    fun getChannelMembers(channel: Channel): Map<User, Role>

    fun joinChannel(
        user: User,
        channel: Channel,
        role: Role,
    ): Channel

    fun updateChannelName(
        channel: Channel,
        name: String,
    ): Channel

    fun leaveChannel(
        user: User,
        channel: Channel,
    ): Channel

    fun clear(): Unit
}
