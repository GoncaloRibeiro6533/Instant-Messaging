interface InvitationRepository {

    fun createInvitation(sender: User, receiver: User, channel: Channel): Invitation

    fun getInvitationDetails(invitationId: Int): Invitation

    fun acceptInvitation(invitationId: Int)

}