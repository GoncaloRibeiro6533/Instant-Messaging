interface InvitationRepository {
    fun createRegisterInvitation(
        sender: User,
        email: String,
        channel: Channel?,
        role: Role?,
    ): RegisterInvitation

    fun createChannelInvitation(
        sender: User,
        receiver: User,
        channel: Channel,
        role: Role,
    ): ChannelInvitation

    fun findRegisterInvitationById(invitationId: Int): Invitation?

    fun findChannelInvitationById(invitationId: Int): Invitation?

    fun updateRegisterInvitation(invitationId: Int): Invitation

    fun updateChannelInvitation(invitationId: Int): Invitation

    fun deleteRegisterInvitationById(invitationId: Int): Invitation

    fun deleteChannelInvitationById(invitationId: Int): Invitation

    fun getInvitationsOfUser(userId: Int): List<Invitation>
}
