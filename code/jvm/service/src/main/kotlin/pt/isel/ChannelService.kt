@file:Suppress("ktlint")
package pt.isel

import jakarta.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    data object UserNotInChannel : ChannelError()
}

/**
 * Service responsible for managing channels.
 * @property trxManager the transaction manager
 * @constructor Creates a ChannelService with the provided transaction manager.
 *
 */
@Named
class ChannelService(
    private val trxManager: TransactionManager,
    private val emitter: UpdatesEmitter,
) {
    fun getChannelById(user: User, id: Int): Either<ChannelError, Channel> =
        trxManager.run {
            if (id < 0) return@run failure(ChannelError.NegativeIdentifier)
            val channel = channelRepo.findById(id) ?: return@run failure(ChannelError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            if (user !in members.keys) return@run failure(ChannelError.Unauthorized)
           // val userRole = channelRepo.getChannelMembers(channel).entries.firstOrNull { member-> member.key == user }.value
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
            val channels = channelRepo.getChannelByName(name, limit, skip) //TODO maybe filter channels on repo, we only want the public ones and the ones the user is in
            val userChannels = channelRepo.getChannelsOfUser(user)
            val filteredChannels = channels.filter { userChannels.containsKey(it) || it.visibility == Visibility.PUBLIC }
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
            channelRepo.joinChannel(user, channel, Role.READ_WRITE)
            return@run success(channel)
        }

    // TODO: Implement pagination
    fun getChannelMembers(channelId: Int): Either<ChannelError, Map<User, Role>> =
        trxManager.run {
            if (channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val channel = channelRepo.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            return@run success(members)
        }

    // TODO: Implement pagination
    fun getChannelsOfUser(userId: Int): Either<ChannelError, Map<Channel,Role>> =
        trxManager.run {
            if (userId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channels = channelRepo.getChannelsOfUser(user)
            return@run success(channels)
        }


    fun joinChannel(
        userToAdd: Int,
        channelId: Int,
        role: Role,
    ): Either<ChannelError, Channel> =
        trxManager.run {
            if (userToAdd < 0 || channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val userToAddInfo = userRepo.findById(userToAdd) ?: return@run failure(ChannelError.UserNotFound)
            val channel =
                channelRepo.findById(channelId)
                    ?: return@run failure(ChannelError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            if (members.contains(userToAddInfo)) {
                return@run failure(ChannelError.UserAlreadyInChannel)
            }
            val updatedChannel = channelRepo.joinChannel(userToAddInfo, channel, role)
            CoroutineScope(Dispatchers.IO).launch {
                emitter.sendEventOfNewMember(channel, userToAddInfo, role, members.keys)
            }
            return@run success(updatedChannel)
        }

    fun updateChannelName(
        channelId: Int,
        name: String,
        userId: Int,
    ): Either<ChannelError, Channel> =
        trxManager.run {
            val user = userRepo.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            if (channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val channel = channelRepo.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            if (!members.contains(user)) return@run failure(ChannelError.Unauthorized)
            if (name.isBlank()) return@run failure(ChannelError.InvalidChannelName)
            val c = channelRepo.getChannelByName(name, 1, 0)
            if (c.isNotEmpty()) return@run failure(ChannelError.ChannelNameAlreadyExists)
            val updatedChannel = channelRepo.updateChannelName(channel, name)
            CoroutineScope(Dispatchers.IO).launch {
                emitter.sendEventOfChannelNameUpdated(updatedChannel, members.keys)
            }
            return@run success(updatedChannel)
        }

    fun leaveChannel(
        userId: Int,
        channelId: Int,
    ): Either<ChannelError, Channel> =
        trxManager.run {
            if (userId < 0 || channelId < 0) return@run failure(ChannelError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ChannelError.UserNotFound)
            val channel = channelRepo.findById(channelId) ?: return@run failure(ChannelError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            if (!members.contains(user)) return@run failure(ChannelError.UserNotInChannel)
            val result = channelRepo.leaveChannel(user, channel) // TODO rename to removeUserFromChannel
            CoroutineScope(Dispatchers.IO).launch {
                emitter.sendEventOfMemberExited(channel, user, members.keys)
            }
            return@run success(result)
        }

}
