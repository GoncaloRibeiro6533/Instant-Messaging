package pt.isel

import jakarta.inject.Named

sealed class InvitationError {
    data object InvitationNotFound : InvitationError()

    data object InvalidEmail : InvitationError()

    data object InvalidRole : InvitationError()

    data object InvalidChannel : InvitationError()

    data object NegativeIdentifier : InvitationError()

    data object InvitationExpired : InvitationError()

    data object InvalidReceiver : InvitationError()

    data object Unauthorized : InvitationError()

    data object UserNotFound : InvitationError()

    data object SenderDoesntBelongToChannel : InvitationError()

    data object AlreadyInChannel : InvitationError()

    data object InvitationAlreadyUsed : InvitationError()

    data object CantInviteToPublicChannel : InvitationError()

    data object ChannelNotFound : InvitationError()
}

@Named
class InvitationService(private val trxManager: TransactionManager) {
    fun getInvitationsOfUser(userId: Int): Either<InvitationError, List<Invitation>> =
        trxManager.run {
            if (userId < 0) return@run failure(InvitationError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(InvitationError.UserNotFound)
            val invitations = invitationRepo.getInvitationsOfUser(user)
            return@run success(invitations)
        }

    fun getRegisterInvitationById(invitationId: Int): Either<InvitationError, Invitation> =
        trxManager.run {
            val invitation =
                invitationRepo.findRegisterInvitationById(invitationId)
                    ?: return@run failure(InvitationError.InvitationNotFound)
            return@run success(invitation)
        }

    fun createRegisterInvitation(
        senderId: Int,
        email: String,
        channelId: Int,
        role: Role,
    ): Either<InvitationError, RegisterInvitation> =
        trxManager.run {
            if (senderId < 0) return@run failure(InvitationError.NegativeIdentifier)
            val user = userRepo.findById(senderId) ?: return@run failure(InvitationError.Unauthorized)
            if (email.isBlank()) return@run failure(InvitationError.InvalidEmail)
            if (channelId < 0) return@run failure(InvitationError.NegativeIdentifier)
            val channel =
                channelRepo.findById(channelId) ?: return@run failure(InvitationError.ChannelNotFound)
            val createdInvitation =
                invitationRepo.createRegisterInvitation(
                    user,
                    email,
                    channel,
                    role,
                )
            return@run success(createdInvitation)
        }

    fun createChannelInvitation(
        senderId: Int,
        receiverId: Int,
        channelId: Int,
        role: Role,
    ): Either<InvitationError, ChannelInvitation> =
        trxManager.run {
            if (senderId < 0) return@run failure(InvitationError.NegativeIdentifier)
            if (receiverId < 0) return@run failure(InvitationError.NegativeIdentifier)
            if (channelId < 0) return@run failure(InvitationError.NegativeIdentifier)
            if (role !in Role.entries.toTypedArray()) return@run failure(InvitationError.InvalidRole)
            val authenticatedUser = userRepo.findById(senderId) ?: return@run failure(InvitationError.Unauthorized)
            val receiver = userRepo.findById(receiverId) ?: return@run failure(InvitationError.InvalidReceiver)
            val channel = channelRepo.findById(channelId) ?: return@run failure(InvitationError.ChannelNotFound)
            if (authenticatedUser == receiver) return@run failure(InvitationError.InvalidReceiver)
            if (channel.visibility == Visibility.PUBLIC) return@run failure(InvitationError.CantInviteToPublicChannel)
            val channelMembers = channelRepo.getChannelMembers(channel).map { userRepo.findById(it) }
            if (channelMembers.none { it == authenticatedUser }) return@run failure(InvitationError.SenderDoesntBelongToChannel)
            if (channelMembers.any { it == receiver }) return@run failure(InvitationError.AlreadyInChannel)
            val createdInvitation =
                invitationRepo.createChannelInvitation(
                    authenticatedUser,
                    receiver,
                    channel,
                    role,
                )

            return@run success(createdInvitation)
        }

    fun acceptChannelInvitation(invitationId: Int): Either<InvitationError, Channel> =
        trxManager.run {
            val invitation: ChannelInvitation =
                (
                    invitationRepo.findChannelInvitationById(invitationId)
                        ?: return@run failure(InvitationError.InvitationNotFound)
                ) as ChannelInvitation
            if (invitation.isUsed) return@run failure(InvitationError.InvitationAlreadyUsed)
            val channel =
                channelRepo.addUserToChannel(invitation.receiver, invitation.channel, invitation.role)
            invitationRepo.updateChannelInvitation(invitation)
            return@run success(channel)
        }

    fun declineChannelInvitation(invitationId: Int): Either<InvitationError, Boolean> =
        trxManager.run {
            invitationRepo.findChannelInvitationById(invitationId) ?: failure(InvitationError.InvitationNotFound)
            val declined = invitationRepo.deleteChannelInvitationById(invitationId)
            return@run success(declined)
        }
}
