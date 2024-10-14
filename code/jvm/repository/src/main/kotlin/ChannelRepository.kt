

interface ChannelRepository {
    fun findById(id: Int): Channel?

    fun getChannelByName(name: String): Channel?

    fun createChannel(
        name: String,
        creator: User,
        visibility: Visibility,
    ): Channel

    fun getChannelsOfUser(user: User): List<Channel>

    fun getChannelMembers(channel: Channel): List<Int>

    fun addUserToChannel(
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
}
