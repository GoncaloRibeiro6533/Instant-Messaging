
class ChannelServices(private val channelRepository : ChannelRepository) {

    fun getChannelById(id: Int) : Channel {
        return channelRepository.getChannelById(id)
    }

    fun createChannel(name: String, visibility: Visibility) : Channel {
        return channelRepository.createChannel(name, visibility)
    }

    fun createInvitation(channelId: Int, userId: Int) : Invitation {
        return channelRepository.createInvitation(channelId, userId)
    }

    fun getMsgHistory(channelId: Int, limit: Int, skip: Int) : List<Message> {
        return channelRepository.getMsgHistory(channelId, limit, skip)
    }

    fun getChannelsOfUser(userId: Int) : List<Channel> {
        return channelRepository.getChannelsOfUser(userId)
    }
}