

sealed class InvitationError {
    data object InvitationNotFound : InvitationError()

    data object InvalidEmail : InvitationError()

    data object InvalidRole : InvitationError()

    data object InvalidChannel : InvitationError()

    data object NegativeIdentifier : InvitationError()

    data object InvitationExpired : InvitationError()

    data object InvalidSender : InvitationError()

    data object InvalidReceiver : InvitationError()

    data object InvalidIsUsed : InvitationError()

    data object Unauthorized : InvitationError()

    data object UserNotFound : InvitationError()

    data object SenderDoesntBelongToChannel : InvitationError()

    data object AlreadyInChannel : InvitationError()

    data object InvitationAlreadyUsed : InvitationError()

    data object CantInviteToPublicChannel : InvitationError()

    data object ChannelNotFound : InvitationError()
}

class InvitationService(private val trxManager: TransactionManager) {
    fun getInvitationsOfUser(
        userId: Int,
        token: String,
    ): Either<InvitationError, List<Invitation>> =
        trxManager.run {
            val user = userRepo.findByToken(token) ?: return@run Either.Left(InvitationError.Unauthorized)
            if (userId < 0) return@run Either.Left(InvitationError.NegativeIdentifier)
            if (user.id != userId) return@run Either.Left(InvitationError.UserNotFound)
            val invitations = invitationRepo.getInvitationsOfUser(userId)
            return@run Either.Right(invitations)
        }

    fun getRegisterInvitationById(invitationId: Int): Either<InvitationError, Invitation> =
        trxManager.run {
            val invitation =
                invitationRepo.findRegisterInvitationById(invitationId)
                    ?: return@run Either.Left(InvitationError.InvitationNotFound)
            return@run Either.Right(invitation)
        }

    fun createRegisterInvitation(
        senderId: Int,
        email: String,
        channelId: Int?,
        role: String?,
        token: String,
    ): Either<InvitationError, RegisterInvitation> =
        trxManager.run {
            userRepo.findByToken(token) ?: return@run Either.Left(InvitationError.Unauthorized)
            if (senderId < 0) return@run Either.Left(InvitationError.NegativeIdentifier)
            if (email.isBlank()) return@run Either.Left(InvitationError.InvalidEmail)
            if (channelId != null && channelId < 0) return@run Either.Left(InvitationError.NegativeIdentifier)
            val roleInvite = Role.entries.find { it.name == role }
            if (role != null && roleInvite == null) return@run Either.Left(InvitationError.InvalidRole)
            val sender = userRepo.findById(senderId) ?: return@run Either.Left(InvitationError.InvalidSender)
            val channel = if (channelId != null) channelRepo.findById(channelId) else null
            if (channelId != null && channel == null) return@run Either.Left(InvitationError.ChannelNotFound)
            val createdInvitation =
                invitationRepo.createRegisterInvitation(
                    sender,
                    email,
                    channel,
                    roleInvite,
                )
            return@run Either.Right(createdInvitation)
        }

    fun createChannelInvitation(
        senderId: Int,
        receiverId: Int,
        channelId: Int,
        role: String,
        token: String,
    ): Either<InvitationError, ChannelInvitation> =
        trxManager.run {
            userRepo.findByToken(token) ?: return@run Either.Left(InvitationError.Unauthorized)
            if (senderId < 0) return@run Either.Left(InvitationError.NegativeIdentifier)
            if (receiverId < 0) return@run Either.Left(InvitationError.NegativeIdentifier)
            if (channelId < 0) return@run Either.Left(InvitationError.NegativeIdentifier)
            if (role.isBlank()) return@run Either.Left(InvitationError.InvalidRole)
            if (role !in Role.entries.map { it.name }) return@run Either.Left(InvitationError.InvalidRole)
            val sender = userRepo.findById(senderId) ?: return@run Either.Left(InvitationError.InvalidSender)
            val receiver = userRepo.findById(receiverId) ?: return@run Either.Left(InvitationError.InvalidReceiver)
            val channel = channelRepo.findById(channelId) ?: return@run Either.Left(InvitationError.ChannelNotFound)
            if (sender == receiver) return@run Either.Left(InvitationError.InvalidReceiver)
            if (channel.visibility == Visibility.PUBLIC) return@run Either.Left(InvitationError.CantInviteToPublicChannel)
            val channelMembers = channelRepo.getChannelMembers(channel).map { userRepo.findById(it) }
            if (channelMembers.none { it == sender }) return@run Either.Left(InvitationError.SenderDoesntBelongToChannel)
            if (channelMembers.any { it == receiver }) return@run Either.Left(InvitationError.AlreadyInChannel)
            val createdInvitation =
                invitationRepo.createChannelInvitation(
                    sender,
                    receiver,
                    channel,
                    Role.valueOf(role),
                )
            return@run Either.Right(createdInvitation)
        }

    fun acceptRegisterInvitation(invitationId: Int): Either<InvitationError, Invitation> =
        trxManager.run {
            val invitation =
                invitationRepo.findRegisterInvitationById(invitationId)
                    ?: return@run Either.Left(InvitationError.InvitationNotFound)
            if (invitation.isUsed) return@run Either.Left(InvitationError.InvitationExpired)
            val invitationEdited = invitationRepo.updateRegisterInvitation(invitationId)
            return@run Either.Right(invitationEdited)
        }

    fun acceptChannelInvitation(
        invitationId: Int,
        token: String,
    ): Either<InvitationError, List<Channel>> =
        trxManager.run {
            val user = userRepo.findByToken(token) ?: return@run Either.Left(InvitationError.Unauthorized)
            val invitation: ChannelInvitation =
                (
                    invitationRepo.findChannelInvitationById(invitationId)
                        ?: return@run Either.Left(InvitationError.InvitationNotFound)
                ) as ChannelInvitation
            if (invitation.isUsed) return@run Either.Left(InvitationError.InvitationAlreadyUsed)
            channelRepo.findById(invitation.channel.id)
                ?: return@run Either.Left(InvitationError.ChannelNotFound)
            val invitationEdited = invitationRepo.updateChannelInvitation(invitationId)
            val channel =
                channelRepo.addUserToChannel(invitation.receiver, invitation.channel, invitation.role)
                    ?: return@run Either.Left(InvitationError.ChannelNotFound)
            val channels = channelRepo.getChannelsOfUser(user)
            return@run Either.Right(channels)
        }
}
