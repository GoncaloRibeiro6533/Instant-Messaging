
sealed class ChannelError {
    data object ChannelNotFound : ChannelError()
    data object InvalidChannelName : ChannelError()
    data object ChannelAlreadyExists : ChannelError()
    data object InvalidVisibility : ChannelError()
    data object NegativeIdentifier : ChannelError()
    data object UserNotFound : ChannelError()
    data object UserAlreadyInChannel : ChannelError()
}


class ChannelService(private val trxManager: TransactionManager) {

    fun getChannelById(id: Int) : Either<ChannelError, Channel> = trxManager.run {
        if (id < 0) return@run Either.Left(ChannelError.NegativeIdentifier)
        val channel = channelRepo.findById(id) ?: return@run Either.Left(ChannelError.ChannelNotFound)
        return@run Either.Right(channel)
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
        //todo adicionar o user ao canal por default aqui?
        return@run Either.Right(channelRepo.createChannel(name, creatorId, visibility))
    }


    fun getChannelMembers(channelId: Int) : Either<ChannelError, List<User>> = trxManager.run {
        if (channelId < 0) return@run Either.Left(ChannelError.NegativeIdentifier)
        if(channelRepo.findById(channelId) == null) return@run Either.Left(ChannelError.ChannelNotFound)
        val members = channelRepo.getChannelMembers(channelId).mapNotNull { userRepo.findById(it) }
        return@run Either.Right(members)
    }

    fun getChannelsOfUser(userId: Int): Either<ChannelError, List<Channel>> = trxManager.run {
        if (userId < 0) return@run Either.Left(ChannelError.NegativeIdentifier)
        if(userRepo.findById(userId) == null) return@run Either.Left(ChannelError.UserNotFound)
        val channels = channelRepo.getChannelsOfUser(userId)
        return@run if (channels != emptyList<Channel>()) Either.Right(channels) else Either.Right(emptyList<Channel>())
    }


    fun addUserToChannel(userId: Int, channelId: Int, role: Role) : Either<ChannelError, Channel?> = trxManager.run {
        if (userId < 0 || channelId < 0) return@run Either.Left(ChannelError.NegativeIdentifier)
        if(userRepo.findById(userId) == null) return@run Either.Left(ChannelError.UserNotFound)
        if(channelRepo.findById(channelId) == null) return@run Either.Left(ChannelError.ChannelNotFound)
        if (channelRepo.getChannelMembers(channelId).contains(userId)) return@run Either.Left(ChannelError.UserAlreadyInChannel)
        return@run Either.Right(channelRepo.addUserToChannel(userId, channelId, role))
    }


}