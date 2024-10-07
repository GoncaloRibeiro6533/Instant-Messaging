interface InvitationRepository {

    fun createInvitation(senderId: Int, receiverId: Int, channelId: Int): Invitation

    fun findById(invitationId: Int): Invitation?

    fun acceptInvitation(invitationId: Int) : Invitation

    fun deleteById(invitationId: Int) : Invitation

    fun getInvitationsOfUser(userId: Int): List<Invitation>

}