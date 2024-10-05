package mocks

import Channel
import Invitation
import InvitationRepository
import User
import Visibility
import java.time.LocalDateTime

class MockInvitationRepo : InvitationRepository {

    companion object {
        val maria = User(1, "Maria", "token1")
        val ricardo = User(2, "Ricardo", "token2")

        val ines = User(3, "Inês", "token3")


        val channel1 = Channel(1, "Aniversário da Maria", maria.id, Visibility.PUBLIC, emptyList(), emptyMap())
        val channel2 = Channel(2, "Aniversário do Ricardo", ricardo.id, Visibility.PUBLIC, emptyList(), emptyMap())

        val invitation1 = Invitation(1, maria.id, ricardo.id, channel1.id, false, LocalDateTime.now())
        val invitation2 = Invitation(2, ricardo.id, maria.id, channel2.id, false, LocalDateTime.now())

        val invitation3 = Invitation(3, maria.id, ricardo.id, channel1.id, false, LocalDateTime.now())

        val invitations = mutableListOf(
            invitation1,
            invitation2
        )
    }

    override fun createInvitation(senderId: Int, receiverId: Int, channelId: Int): Invitation {
        val newInvitation =
            Invitation(invitations.size + 1, senderId, receiverId, channelId, false, LocalDateTime.now())
        invitations.add(newInvitation)
        return newInvitation
    }

    override fun getInvitationDetails(invitationId: Int): Invitation? {
        return invitations.firstOrNull { it.id == invitationId }
    }

    override fun acceptInvitation(invitationId: Int): Invitation {
        val invitation = invitations.first { it.id == invitationId }
        val updatedInvitation = invitation.copy(isUsed = true)
        invitations.remove(invitation)
        invitations.add(updatedInvitation)
        return updatedInvitation
    }

    override fun declineInvitation(invitationId: Int): Invitation {
        val invitation = invitations.first { it.id == invitationId }
        invitations.remove(invitation)
        return invitation
    }

    override fun getInvitationsOfUser(userId: Int): List<Invitation> {
        return invitations.filter { it.receiverId == userId }
    }

}