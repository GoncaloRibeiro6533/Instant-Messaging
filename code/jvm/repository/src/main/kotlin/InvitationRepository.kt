interface InvitationRepository {

    fun createInvitation(senderId: Int, receiverId: Int, channelId: Int): Invitation

    fun getInvitationDetails(invitationId: Int): Invitation?

    fun acceptInvitation(invitationId: Int) : Invitation

}