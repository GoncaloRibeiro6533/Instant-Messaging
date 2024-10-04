
interface ChannelRepository {

   fun getChannelById(id: Int): Channel?

   fun getChannelByName(name: String): Channel?

   fun createChannel(name: String, creatorId: Int, visibility: Visibility): Channel

   fun getMsgHistory(channelId: Int, limit: Int, skip: Int): List<Message>

   fun getChannelsOfUser(userId: Int): List<Channel>

}