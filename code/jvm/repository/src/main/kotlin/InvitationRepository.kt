interface InvitationRepository {

    fun createInvitation(sender: Int, receiver: Int, channel: Int): Invitation

    fun getInvitationDetails(invitationId: Int): Invitation

    fun acceptInvitation(invitationId: Int) : Invitation

}