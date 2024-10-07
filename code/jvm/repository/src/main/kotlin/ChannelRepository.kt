
interface ChannelRepository {

   fun findById(id: Int): Channel?

   fun getChannelByName(name: String): Channel?

   fun createChannel(name: String, creator: User, visibility: Visibility): Channel

   fun getChannelsOfUser(userId: Int): List<Channel>

   fun getChannelMembers(channelId: Int): List<Int>

   fun addUserToChannel(userId: Int, channelId: Int, role: Role): Channel?

}