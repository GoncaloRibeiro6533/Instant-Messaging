
sealed class ChannelError {
    data object ChannelNotFound : ChannelError()
    data object InvalidChannelName : ChannelError()
    data object ChannelAlreadyExists : ChannelError()
    data object InvalidVisibility : ChannelError()
    data object NegativeIdentifier : ChannelError()
    data object InvalidChannelId : ChannelError()
}



class ChannelServices(private val trxManager: TransactionManager) {

    fun getChannelById(id: Int) : Either<ChannelError, Channel> = trxManager.run {
        if (id < 0) return@run Either.Left(ChannelError.NegativeIdentifier)
        val channel = channelRepo.findById(id)
        return@run if (channel != null) Either.Right(channel) else Either.Left(ChannelError.ChannelNotFound)
    }

    fun getChannelByName(name: String) : Either<ChannelError, Channel> = trxManager.run {
        if (name.isBlank()) return@run Either.Left(ChannelError.InvalidChannelName)
        val channel = channelRepo.getChannelByName(name)
        return@run if (channel != null) Either.Right(channel) else Either.Left(ChannelError.ChannelNotFound)
    }

    fun createChannel(name: String, creatorId: Int, visibility: Visibility) : Either<ChannelError, Channel> = trxManager.run {
        if (name.isBlank()) return@run Either.Left(ChannelError.InvalidChannelName)
        if (!Visibility.entries.toTypedArray().contains(visibility))
            return@run Either.Left(ChannelError.InvalidVisibility)
        if (channelRepo.getChannelByName(name) != null)
            return@run Either.Left(ChannelError.ChannelAlreadyExists)
        return@run Either.Right(channelRepo.createChannel(name, creatorId, visibility))
    }

    fun getMsgHistory(channelId: Int, limit: Int = 5, skip: Int=5) : Either<ChannelError, List<Message>> = trxManager.run {
        if (channelId < 0) return@run Either.Left(ChannelError.NegativeIdentifier)
        //todo maybe user must be authenticated to see messages?
        return@run Either.Right(channelRepo.getMsgHistory(channelId, limit, skip))
    }

    fun getChannelMembers(channelId: Int) : Either<ChannelError, List<User>> = trxManager.run {
        if (channelId < 0) return@run Either.Left(ChannelError.NegativeIdentifier)
        val members = channelRepo.getChannelMembers(channelId)
        return@run if (members != null) Either.Right(members) else Either.Left(ChannelError.ChannelNotFound)
    }

    fun getChannelsOfUser(userId: Int): Either<ChannelError, List<Channel>> = trxManager.run {
        if (userId < 0) return@run Either.Left(ChannelError.NegativeIdentifier)
        val channels = channelRepo.getChannelsOfUser(userId)
        return@run if (channels != null) Either.Right(channels) else Either.Left(ChannelError.ChannelNotFound)
    }
}