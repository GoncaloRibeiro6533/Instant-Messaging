import jakarta.inject.Named

sealed class ChannelError {
    data object ChannelNotFound : ChannelError()

    data object InvalidChannelName : ChannelError()

    data object ChannelAlreadyExists : ChannelError()

    data object InvalidVisibility : ChannelError()

    data object NegativeIdentifier : ChannelError()

    data object UserNotFound : ChannelError()

    data object UserAlreadyInChannel : ChannelError()

    data object Unauthorized : ChannelError()

    data object ChannelNameAlreadyExists : ChannelError()
}

@Named
class ChannelService(private val trxManager: TransactionManager) {
    fun getChannelById(id: Int): Either<ChannelError, Channel> =
        trxManager.run {
            if (id < 0) return@run failure(ChannelError.NegativeIdentifier)
            val channel = channelRepo.findById(id) ?: return@run failure(ChannelError.ChannelNotFound)
            return@run success(channel)
        }

    fun getChannelByName(name: String): Either<ChannelError, Channel> =
        trxManager.run {
            if (name.isBlank()) return@run failure(ChannelError.InvalidChannelName)
            val channel = channelRepo.getChannelByName(name)
            return@run if (channel != null) success(channel) else failure(ChannelError.ChannelNotFound)
        }

    fun createChannel(
        name: String,
        creatorId: Int,
        visibility: Visibility,
        // token: String,
    ): Either<ChannelError, Channel> =
        trxManager.run {
            if (name.isBlank()) return@run failure(ChannelError.InvalidChannelName)
            // userRepo.findByToken(token) ?: return@run failure(ChannelError.Unauthorized)
            val user = userRepo.findById(creatorId) ?: return@run failure(ChannelError.UserNotFound)
            if (!Visibility.entries.toTypedArray().contains(visibility)) {
                return@run failure(ChannelError.InvalidVisibility)
            }
            if (channelRepo.getChannelByName(name) != null) {
                return@run failure(ChannelError.ChannelAlreadyExists)
            }
            val channel = channelRepo.createChannel(name, user, visibility)
            channelRepo.addUserToChannel(user, channel, Role.READ_WRITE)
            return@run success(channel)
        }

    fun getChannelMembers(channelId: Int): Either<ChannelError, List<User>> =
        trxManager.run {
            if (channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val channel = channelRepo.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel).mapNotNull { userRepo.findById(it) }
            return@run success(members)
        }

    fun getChannelsOfUser(userId: Int): Either<ChannelError, List<Channel>> =
        trxManager.run {
            if (userId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channels = channelRepo.getChannelsOfUser(user)
            return@run success(channels)
        }

    fun addUserToChannel(
        userId: Int,
        channelId: Int,
        role: Role,
    ): Either<ChannelError, Channel> =
        trxManager.run {
            if (userId < 0 || channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channel =
                channelRepo.findById(channelId)
                    ?: return@run failure(ChannelError.ChannelNotFound)
            if (channelRepo.getChannelMembers(channel).contains(userId)) {
                return@run failure(ChannelError.UserAlreadyInChannel)
            }
            return@run success(channelRepo.addUserToChannel(user, channel, role))
        }

    fun updateChannelName(
        channelId :Int,
        name: String,
    ): Either<ChannelError, Channel> =
        trxManager.run {
            if (channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val channel = channelRepo.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            if(name.isBlank()) return@run failure(ChannelError.InvalidChannelName)
            if(channelRepo.getChannelByName(name) != null) return@run failure(ChannelError.ChannelNameAlreadyExists)
            return@run success(channelRepo.updateChannelName(channel, name))
        }

    fun leaveChannel(
        userId: Int,
        channelId: Int,
    ): Either<ChannelError, Channel> =
        trxManager.run {
            if (userId < 0 || channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channel = channelRepo.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            return@run success(channelRepo.leaveChannel(user, channel))
        }
}
