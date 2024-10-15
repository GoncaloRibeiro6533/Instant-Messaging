import org.jdbi.v3.core.Handle

class JdbiInvitationRepository(
    private val handle: Handle,
) : InvitationRepository {
    override fun createRegisterInvitation(
        sender: User,
        email: String,
        channel: Channel,
        role: Role,
    ): RegisterInvitation {
        TODO("Not yet implemented")
    }

    override fun createChannelInvitation(
        sender: User,
        receiver: User,
        channel: Channel,
        role: Role,
    ): ChannelInvitation {
        TODO("Not yet implemented")
    }

    override fun findRegisterInvitationById(invitationId: Int): Invitation? {
        TODO("Not yet implemented")
    }

    override fun findChannelInvitationById(invitationId: Int): Invitation? {
        TODO("Not yet implemented")
    }

    override fun updateRegisterInvitation(invitation: Invitation): Invitation {
        TODO("Not yet implemented")
    }

    override fun updateChannelInvitation(invitation: Invitation): Invitation {
        TODO("Not yet implemented")
    }

    override fun deleteRegisterInvitationById(invitationId: Int): Invitation {
        TODO("Not yet implemented")
    }

    override fun deleteChannelInvitationById(invitationId: Int): Invitation {
        TODO("Not yet implemented")
    }

    override fun getInvitationsOfUser(user: User): List<Invitation> {
        TODO("Not yet implemented")
    }
}
