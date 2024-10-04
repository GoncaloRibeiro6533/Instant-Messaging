class MockInvitationRepo : InvitationRepo {

    companion object {
        val maria = User(1, "Maria", "token1", emptyList(), emptyList())
        val ricardo = User(2, "Ricardo", "token2", emptyList(), emptyList())

        val channel1 = Channel(1, "Aniversário da Maria", maria, Visibility.PUBLIC, emptyList(), emptyMap())
        val channel2 = Channel(2, "Aniversário do Ricardo", ricardo, Visibility.PUBLIC, emptyList(), emptyMap())

        val invitation1 = Invitation(1, maria, ricardo, channel1, false, LocalDateTime.now())
        val invitation2 = Invitation(2, ricardo, maria, channel2, false, LocalDateTime.now())

        val invitations = mutableListOf(
            invitation1,
            invitation2
        )
    }

    override fun createInvitation(senderId: Int, receiverId: Int, channelId: Int): Invitation {
        val newInvitation = Invitation(invitations.size + 1, senderId, receiverId, channelId, false, LocalDateTime.now())
        invitations.add(newInvitation)
        return newInvitation
    }

    override fun getInvitationDetails(invitationId: Int): Invitation? {
        return invitations.firstOrNull { it.id == invitationId }
    }

    override fun acceptInvitation(invitationId: Int): Invitation {
        val invitation = invitations.first { it.id == invitationId }
        val updatedInvitation = invitation.copy(isUsed = true)
        invitations[invitations.indexOf(invitation)] = updatedInvitation
        return updatedInvitation
    }


}