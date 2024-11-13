package pt.isel

import jakarta.inject.Named

sealed class ChannelError {
    data object ChannelNotFound : ChannelError()

    data object InvalidChannelName : ChannelError()

    data object InvalidVisibility : ChannelError()

    data object NegativeIdentifier : ChannelError()

    data object UserNotFound : ChannelError()

    data object UserAlreadyInChannel : ChannelError()

    data object ChannelNameAlreadyExists : ChannelError()

    data object InvalidSkip : ChannelError()

    data object InvalidLimit : ChannelError()

    data object Unauthorized : ChannelError()

}

/**
 * Service responsible for managing channels.
 * @property trxManager the transaction manager
 * @constructor Creates a ChannelService with the provided transaction manager.
 *
 */
@Named
class ChannelService(private val trxManager: TransactionManager) {
    fun getChannelById(id: Int): Either<ChannelError, Channel> =
        trxManager.run {
            if (id < 0) return@run failure(ChannelError.NegativeIdentifier)
            val channel = channelRepo.findById(id) ?: return@run failure(ChannelError.ChannelNotFound)
            return@run success(channel)
        }

    fun getChannelByName(
        userId: Int,
        name: String,
        limit: Int = 10,
        skip: Int = 0,
    ): Either<ChannelError, List<Channel>> =
        trxManager.run {
            if (name.isBlank()) return@run failure(ChannelError.InvalidChannelName)
            if (limit < 0) return@run failure(ChannelError.InvalidLimit)
            if (skip < 0) return@run failure(ChannelError.InvalidSkip)
            val user = userRepo.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channels = channelRepo.getChannelByName(name, limit, skip)
            val userChannels = channelRepo.getChannelsOfUser(user)
            val filteredChannels = channels.filter { userChannels.contains(it) || it.visibility == Visibility.PUBLIC }
            return@run success(filteredChannels)
        }

    fun createChannel(
        name: String,
        creatorId: Int,
        visibility: Visibility,
    ): Either<ChannelError, Channel> =
        trxManager.run {
            if (name.isBlank()) return@run failure(ChannelError.InvalidChannelName)
            val user = userRepo.findById(creatorId) ?: return@run failure(ChannelError.UserNotFound)
            if (!Visibility.entries.toTypedArray().contains(visibility)) {
                return@run failure(ChannelError.InvalidVisibility)
            }
            if (channelRepo.getChannelByName(name, 1, 0).isNotEmpty()) {
                return@run failure(ChannelError.ChannelNameAlreadyExists)
            }
            val channel = channelRepo.createChannel(name, user, visibility)
            return@run success(channel)
        }

    // TODO: Implement pagination
    //TODO should return User and corresponding role, its already implemented in the repository
    fun getChannelMembers(channelId: Int): Either<ChannelError, List<User>> =
        trxManager.run {
            if (channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val channel = channelRepo.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel).keys.toList()
            return@run success(members)
        }

    // TODO: Implement pagination
    fun getChannelsOfUser(userId: Int): Either<ChannelError, List<Channel>> =
        trxManager.run {
            if (userId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channels = channelRepo.getChannelsOfUser(user)
            return@run success(channels)
        }


    //TODO this operation doesn't make sense
    fun addUserToChannel(
        userToAdd: Int,
        channelId: Int,
        role: Role,
        userAdding: Int
    ): Either<ChannelError, Channel> =
        trxManager.run {
            if (userToAdd < 0 || channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val userToAddInfo = userRepo.findById(userToAdd) ?: return@run failure(ChannelError.UserNotFound)
            val userAdding = userRepo.findById(userAdding) ?: return@run failure(ChannelError.UserNotFound)
            val channel =
                channelRepo.findById(channelId)
                    ?: return@run failure(ChannelError.ChannelNotFound)
            if(!channelRepo.getChannelMembers(channel).contains(userAdding)) return@run failure(ChannelError.Unauthorized)
            if (channelRepo.getChannelMembers(channel).contains(userToAddInfo)) {
                return@run failure(ChannelError.UserAlreadyInChannel)
            }
            return@run success(channelRepo.addUserToChannel(userToAddInfo, channel, role))
        }

    fun updateChannelName(
        channelId: Int,
        name: String,
        userId: Int
    ): Either<ChannelError, Channel> =
        trxManager.run {
            val user = userRepo.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            if (channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val channel = channelRepo.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            if(!channelRepo.getChannelMembers(channel).contains(user)) return@run failure(ChannelError.Unauthorized)
            if (name.isBlank()) return@run failure(ChannelError.InvalidChannelName)
            if (channelRepo.getChannelByName(name, 1, 0).isNotEmpty()) return@run failure(ChannelError.ChannelNameAlreadyExists)
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
