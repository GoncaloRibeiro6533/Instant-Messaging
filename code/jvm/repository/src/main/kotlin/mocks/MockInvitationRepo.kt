package mocks

import Channel
import ChannelInvitation
import Invitation
import InvitationRepository
import RegisterInvitation
import Role
import User
import java.time.LocalDateTime

class MockInvitationRepo : InvitationRepository {
    /*        val maria = User(1, "Maria", "user1@mail.com","token1")
        val ricardo = User(2, "Ricardo", "user1@mail.com","token2")

        val ines = User(3, "Inês", "user1@mail.com","token3")


        val channel1 = Channel(1, "Aniversário da Maria", maria, Visibility.PUBLIC)
        val channel2 = Channel(2, "Aniversário do Ricardo", ricardo, Visibility.PUBLIC)

        val registerInvitation1 = RegisterInvitation(1, maria, ricardo.email, channel1, Role.READ_WRITE,false,
            LocalDateTime.of(2021, 10, 10, 10, 10))
        val registerInvitation2 = RegisterInvitation(2, maria, ines.email, channel1, Role.READ_WRITE,false,
            LocalDateTime.of(2021, 10, 10, 10, 10))

        val channelInvitation = ChannelInvitation(3, maria, ricardo, channel1, Role.READ_WRITE, false,
            LocalDateTime.of(2021, 10, 10, 10, 10))
*/
    private val channelInvitations = mutableSetOf<ChannelInvitation>()
    private var currentIdChannelInvitations = channelInvitations.size

    private val registerInvitations = mutableSetOf<RegisterInvitation>()
    private var currentIdRegisterInvitation = registerInvitations.size

    override fun createRegisterInvitation(
        sender: User,
        email: String,
        channel: Channel?,
        role: Role?,
    ): RegisterInvitation {
        val invitation =
            RegisterInvitation(
                ++currentIdRegisterInvitation,
                sender,
                email,
                channel,
                role,
                false,
                LocalDateTime.now(),
            )
        registerInvitations.add(invitation)
        return invitation
    }

    override fun createChannelInvitation(
        sender: User,
        receiver: User,
        channel: Channel,
        role: Role,
    ): ChannelInvitation {
        val invitation =
            ChannelInvitation(
                ++currentIdChannelInvitations,
                sender,
                receiver,
                channel,
                role,
                false,
                LocalDateTime.now(),
            )
        channelInvitations.add(invitation)
        return invitation
    }

    override fun findRegisterInvitationById(invitationId: Int): Invitation? {
        return registerInvitations.firstOrNull { it.id == invitationId }
    }

    override fun findChannelInvitationById(invitationId: Int): Invitation? {
        return channelInvitations.firstOrNull { it.id == invitationId }
    }

    override fun updateRegisterInvitation(invitationId: Int): Invitation {
        val invitation = registerInvitations.first { it.id == invitationId }
        val invitationEdited = invitation.markAsUsed()
        registerInvitations.remove(invitation)
        registerInvitations.add(invitationEdited)
        return invitationEdited
    }

    override fun updateChannelInvitation(invitationId: Int): Invitation {
        val invitation = channelInvitations.first { it.id == invitationId }
        val invitationEdited = invitation.markAsUsed()
        channelInvitations.remove(invitation)
        channelInvitations.add(invitationEdited)
        return invitationEdited
    }

    override fun deleteChannelInvitationById(invitationId: Int): Invitation {
        val invitation = channelInvitations.first { it.id == invitationId }
        channelInvitations.remove(invitation)
        return invitation
    }

    override fun deleteRegisterInvitationById(invitationId: Int): Invitation {
        val invitation = registerInvitations.first { it.id == invitationId }
        registerInvitations.remove(invitation)
        return invitation
    }

    override fun getInvitationsOfUser(userId: Int): List<Invitation> {
        return channelInvitations.filter { it.receiver.id == userId }
    }
}
