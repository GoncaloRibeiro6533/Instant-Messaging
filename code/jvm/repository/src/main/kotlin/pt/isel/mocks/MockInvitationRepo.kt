package pt.isel.mocks

import pt.isel.Channel
import pt.isel.ChannelInvitation
import pt.isel.InvitationRepository
import pt.isel.RegisterInvitation
import pt.isel.Role
import pt.isel.User
import java.time.LocalDateTime

class MockInvitationRepo : InvitationRepository {
    private val channelInvitations = mutableSetOf<ChannelInvitation>()
    private var currentIdChannelInvitations = channelInvitations.size

    private val registerInvitations = mutableSetOf<RegisterInvitation>()
    private var currentIdRegisterInvitation = registerInvitations.size

    override fun createRegisterInvitation(
        sender: User,
        email: String,
        channel: Channel,
        role: Role,
        timestamp: LocalDateTime,
        code: String,
    ): RegisterInvitation {
        val invitation =
            RegisterInvitation(
                ++currentIdRegisterInvitation,
                sender,
                email,
                channel,
                role,
                false,
                timestamp,
                code,
            )
        registerInvitations.add(invitation)
        return invitation
    }

    override fun createChannelInvitation(
        sender: User,
        receiver: User,
        channel: Channel,
        role: Role,
        timestamp: LocalDateTime,
    ): ChannelInvitation {
        val invitation =
            ChannelInvitation(
                ++currentIdChannelInvitations,
                sender,
                receiver,
                channel,
                role,
                false,
                timestamp,
            )
        channelInvitations.add(invitation)
        return invitation
    }

    override fun findRegisterInvitationById(invitationId: Int): RegisterInvitation? {
        return registerInvitations.firstOrNull { it.id == invitationId }
    }

    override fun findChannelInvitationById(invitationId: Int): ChannelInvitation? {
        return channelInvitations.firstOrNull { it.id == invitationId }
    }

    override fun updateRegisterInvitation(invitation: RegisterInvitation): RegisterInvitation {
        val invite = registerInvitations.first { it.id == invitation.id }
        val invitationEdited = invite.markAsUsed()
        registerInvitations.remove(invite)
        registerInvitations.add(invitationEdited)
        return invitationEdited
    }

    override fun updateChannelInvitation(invitation: ChannelInvitation): ChannelInvitation {
        val invite = channelInvitations.first { it.id == invitation.id }
        val invitationEdited = invite.markAsUsed()
        channelInvitations.remove(invite)
        channelInvitations.add(invitationEdited)
        return invitationEdited
    }

    override fun deleteChannelInvitationById(invitationId: Int): Boolean {
        val invitation = channelInvitations.first { it.id == invitationId }
        channelInvitations.remove(invitation)
        return true
    }

    override fun deleteRegisterInvitationById(invitationId: Int): Boolean {
        val invitation = registerInvitations.first { it.id == invitationId }
        registerInvitations.remove(invitation)
        return true
    }

    override fun getInvitationsOfUser(user: User): List<ChannelInvitation> {
        return channelInvitations.filter { it.receiver.id == user.id }
    }

    override fun findRegisterInvitationByCode(code: String): RegisterInvitation? {
        return registerInvitations.firstOrNull { it.code == code }
    }

    override fun clear() {
        channelInvitations.clear()
        currentIdChannelInvitations = 0
        registerInvitations.clear()
        currentIdRegisterInvitation = 0
    }
}
