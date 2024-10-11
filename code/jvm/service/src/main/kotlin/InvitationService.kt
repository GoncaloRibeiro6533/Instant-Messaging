

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
            val user = userRepo.findByToken(token) ?: return@run failure(InvitationError.Unauthorized)
            if (userId < 0) return@run failure(InvitationError.NegativeIdentifier)
            if (user.id != userId) return@run failure(InvitationError.UserNotFound)
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
        channelId: Int?,
        role: Role?,
        token: String,
    ): Either<InvitationError, RegisterInvitation> =
        trxManager.run {
            if (senderId < 0) {
                return@run failure(InvitationError.NegativeIdentifier)
            }
            val autenticatedUser = userRepo.findByToken(token) ?: return@run failure(InvitationError.Unauthorized)
            if (email.isBlank()) {
                return@run failure(InvitationError.InvalidEmail)
            }
            if (channelId != null && channelId < 0) {
                return@run failure(InvitationError.NegativeIdentifier)
            }
            val channel =
                if (channelId != null) channelRepo.findById(channelId) else null
            if (channelId != null && channel == null) {
                return@run failure(InvitationError.ChannelNotFound)
            }
            if (channel != null && role == null) {
                return@run failure(InvitationError.InvalidRole)
            }
            val createdInvitation =
                invitationRepo.createRegisterInvitation(
                    autenticatedUser,
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
        role: String,
        token: String,
    ): Either<InvitationError, ChannelInvitation> =
        trxManager.run {
            userRepo.findByToken(token) ?: return@run failure(InvitationError.Unauthorized)
            if (senderId < 0) return@run failure(InvitationError.NegativeIdentifier)
            if (receiverId < 0) return@run failure(InvitationError.NegativeIdentifier)
            if (channelId < 0) return@run failure(InvitationError.NegativeIdentifier)
            if (role.isBlank()) return@run failure(InvitationError.InvalidRole)
            if (role !in Role.entries.map { it.name }) return@run failure(InvitationError.InvalidRole)
            val autenticatedUser = userRepo.findByToken(token) ?: return@run failure(InvitationError.Unauthorized)
            val receiver = userRepo.findById(receiverId) ?: return@run failure(InvitationError.InvalidReceiver)
            val channel = channelRepo.findById(channelId) ?: return@run failure(InvitationError.ChannelNotFound)
            if (autenticatedUser == receiver) return@run failure(InvitationError.InvalidReceiver)
            if (channel.visibility == Visibility.PUBLIC) return@run failure(InvitationError.CantInviteToPublicChannel)
            val channelMembers = channelRepo.getChannelMembers(channel).map { userRepo.findById(it) }
            if (channelMembers.none { it == autenticatedUser }) return@run failure(InvitationError.SenderDoesntBelongToChannel)
            if (channelMembers.any { it == receiver }) return@run failure(InvitationError.AlreadyInChannel)
            val createdInvitation =
                invitationRepo.createChannelInvitation(
                    autenticatedUser,
                    receiver,
                    channel,
                    Role.valueOf(role),
                )
            return@run success(createdInvitation)
        }

    fun acceptChannelInvitation(
        invitationId: Int,
        token: String,
    ): Either<InvitationError, Channel> =
        trxManager.run {
            val user = userRepo.findByToken(token) ?: return@run failure(InvitationError.Unauthorized)
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
}
