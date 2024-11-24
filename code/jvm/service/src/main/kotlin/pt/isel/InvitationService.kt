package pt.isel

import jakarta.inject.Named
import java.time.LocalDateTime

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
class InvitationService(
    private val trxManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val emitter: UpdatesEmitter,
) {
    fun getInvitationsOfUser(userId: Int): Either<InvitationError, List<ChannelInvitation>> =
        trxManager.run {
            if (userId < 0) return@run failure(InvitationError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(InvitationError.UserNotFound)
            val invitations = invitationRepo.getInvitationsOfUser(user)
            return@run success(invitations)
        }

    fun getRegisterInvitationById(invitationId: Int): Either<InvitationError, RegisterInvitation> =
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
            if (!usersDomain.isValidEmail(email)) return@run failure(InvitationError.InvalidEmail)
            if (userRepo.findByEmail(email) != null) return@run failure(InvitationError.AlreadyInChannel)
            if (channelId < 0) return@run failure(InvitationError.NegativeIdentifier)
            val channel =
                channelRepo.findById(channelId) ?: return@run failure(InvitationError.ChannelNotFound)
            val createdInvitation =
                invitationRepo.createRegisterInvitation(
                    user,
                    email,
                    channel,
                    role,
                    LocalDateTime.now(),
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
            val channelMembers = channelRepo.getChannelMembers(channel)
            if (channelMembers.none { it.key == authenticatedUser }) return@run failure(InvitationError.SenderDoesntBelongToChannel)
            if (channelMembers.any { it.key == receiver }) return@run failure(InvitationError.AlreadyInChannel)
            val createdInvitation =
                invitationRepo.createChannelInvitation(
                    authenticatedUser,
                    receiver,
                    channel,
                    role,
                    LocalDateTime.now(),
                )
            emitter.sendEventOfNewInvitation(createdInvitation)
            return@run success(createdInvitation)
        }

    fun acceptChannelInvitation(
        invitationId: Int,
        userId: Int,
    ): Either<InvitationError, Channel> =
        trxManager.run {
            userRepo.findById(userId) ?: return@run failure(InvitationError.UserNotFound)
            if (userId < 0) return@run failure(InvitationError.NegativeIdentifier)

            val invitation: ChannelInvitation =
                invitationRepo.findChannelInvitationById(invitationId)
                    ?: return@run failure(InvitationError.InvitationNotFound)
            if (invitation.receiver.id != userId) return@run failure(InvitationError.Unauthorized)
            if (invitation.timestamp.plusDays(1).isBefore(LocalDateTime.now())) return@run failure(InvitationError.InvitationExpired)
            if (invitation.isUsed) return@run failure(InvitationError.InvitationAlreadyUsed)
            val members = channelRepo.getChannelMembers(invitation.channel)
            if (members.any { it.key == invitation.receiver }) return@run failure(InvitationError.AlreadyInChannel)
            val channel =
                channelRepo.joinChannel(invitation.receiver, invitation.channel, invitation.role)
            invitationRepo.updateChannelInvitation(invitation)
            emitter.sendEventOfNewMember(channel, invitation.receiver, invitation.role, members.keys)
            return@run success(channel)
        }

    fun declineChannelInvitation(
        invitationId: Int,
        userId: Int,
    ): Either<InvitationError, Boolean> =
        trxManager.run {
            val invitation =
                invitationRepo.findChannelInvitationById(invitationId)
                    ?: return@run failure(InvitationError.InvitationNotFound)
            userRepo.findById(userId) ?: return@run failure(InvitationError.UserNotFound)
            if (userId < 0) return@run failure(InvitationError.NegativeIdentifier)

            if (invitation.receiver.id != userId) return@run failure(InvitationError.Unauthorized)
            if (invitation.timestamp.plusDays(1).isBefore(LocalDateTime.now())) {
                return@run failure(InvitationError.InvitationExpired)
            }
            val declined = invitationRepo.deleteChannelInvitationById(invitationId)
            return@run success(declined)
        }
}
