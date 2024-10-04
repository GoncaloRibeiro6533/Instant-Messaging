
class InvitationService(private val invitationRepository: InvitationRepository){

    fun createInvitation(senderId: Int, receiverId: Int, channelId: Int): Invitation {
        return invitationRepository.createInvitation(senderId, receiverId, channelId)
    }

    fun getInvitationDetails(id: Int): Invitation? {
        return invitationRepository.getInvitationDetails(id)
    }

    fun acceptInvitation(id: Int) {
        invitationRepository.acceptInvitation(id)
    }

}

