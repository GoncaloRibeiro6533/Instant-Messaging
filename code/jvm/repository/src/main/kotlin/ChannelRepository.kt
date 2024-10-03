
interface ChannelRepository {

   fun getChannelById(id: Int): Channel

   fun createChannel(name: String, visibility: Visibility): Channel

   fun createInvitation(channelId: Int, userId: Int): Invitation

   fun getMsgHistory(channelId: Int, limit: Int, skip: Int): List<Message>

   fun getChannelsOfUser(userId: Int): List<Channel>

}