
sealed class ChannelError {
    data object ChannelNotFound : ChannelError()
    data object InvalidChannelName : ChannelError()
    data object ChannelAlreadyExists : ChannelError()
    data object InvalidVisibility : ChannelError()
    data object NegativeIdentifier : ChannelError()
    data object InvalidChannelId : ChannelError()
}



class ChannelServices(private val channelRepository : ChannelRepository) {

    fun getChannelById(id: Int) : Either<ChannelError, Channel> {
        if (id < 0) return Either.Left(ChannelError.NegativeIdentifier)
        val channel = channelRepository.getChannelById(id)
        return if (channel != null) Either.Right(channel) else Either.Left(ChannelError.ChannelNotFound)
    }

    fun getChannelByName(name: String) : Either<ChannelError, Channel> {
        if (name.isBlank()) return Either.Left(ChannelError.InvalidChannelName)
        val channel = channelRepository.getChannelByName(name)
        return if (channel != null) Either.Right(channel) else Either.Left(ChannelError.ChannelNotFound)
    }

    fun createChannel(name: String, creatorId: Int, visibility: Visibility) : Either<ChannelError, Channel> {
        if (name.isBlank()) return Either.Left(ChannelError.InvalidChannelName)
        if (!Visibility.entries.toTypedArray().contains(visibility))
            return Either.Left(ChannelError.InvalidVisibility)
        if (channelRepository.getChannelByName(name) != null)
            return Either.Left(ChannelError.ChannelAlreadyExists)
        return Either.Right(channelRepository.createChannel(name, creatorId, visibility))
    }

    fun getMsgHistory(channelId: Int, limit: Int = 5, skip: Int=5) : Either<ChannelError, List<Message>> {
        if (channelId < 0) return Either.Left(ChannelError.NegativeIdentifier)
        //todo maybe user must be authenticated to see messages?
        return Either.Right(channelRepository.getMsgHistory(channelId, limit, skip))
    }

    fun getChannelMembers(channelId: Int) : Either<ChannelError, List<User>> {
        if (channelId < 0) return Either.Left(ChannelError.NegativeIdentifier)
        val members = channelRepository.getChannelMembers(channelId)
        return if (members != null) Either.Right(members) else Either.Left(ChannelError.ChannelNotFound)
    }

    fun getChannelsOfUser(userId: Int): Either<ChannelError, List<Channel>> {
        if (userId < 0) return Either.Left(ChannelError.NegativeIdentifier)
        val channels = channelRepository.getChannelsOfUser(userId)
        return if (channels != null) Either.Right(channels) else Either.Left(ChannelError.ChannelNotFound)
    }
}