class ChannelServices(private val channelRepository : ChannelRepository) {

    fun getChannelById(id: Int) : Channel {
        if (id < 0) throw Errors.BadRequestException("Id must be greater than 0")
        return channelRepository.getChannelById(id) ?: throw Errors.NotFoundException("Channel not found")
    }

    fun getChannelByName(name: String) : Channel {
        if (name.isBlank()) throw Errors.BadRequestException("Channel name must not be blank")
        return channelRepository.getChannelByName(name) ?: throw Errors.NotFoundException("Channel not found")
    }

    fun createChannel(name: String, creatorId: Int, visibility: Visibility) : Channel {
        if (name.isBlank()) throw Errors.BadRequestException("Channel name must not be blank")
        if (!Visibility.entries.toTypedArray().contains(visibility))
            throw Errors.BadRequestException("Invalid visibility")
        if (channelRepository.getChannelByName(name) != null)
            throw Errors.BadRequestException("Channel already exists")
        return channelRepository.createChannel(name, creatorId, visibility)
    }

    fun getMsgHistory(channelId: Int, limit: Int = 5, skip: Int=5) : List<Message> {
        if (channelId < 0) throw Errors.BadRequestException("Channel id must be greater than 0")
        //todo maybe user must be authenticated to see messages?
        return channelRepository.getMsgHistory(channelId, limit, skip)
    }

    fun getChannelMembers(channelId: Int) : List<User> {
        if (channelId < 0) throw Errors.BadRequestException("Channel id must be greater than 0")
        return channelRepository.getChannelMembers(channelId) ?: throw Errors.NotFoundException("Channel not found")
    }

    fun getChannelsOfUser(userId: Int): List<Channel> {
        if (userId < 0) throw Errors.BadRequestException("User id must be greater than 0")
        return channelRepository.getChannelsOfUser(userId) ?: throw Errors.NotFoundException("User not found")
    }




}