
class InvitationService(private val invitationRepository: InvitationRepository){

    fun createInvitation(senderId: Int, receiverId: Int, channelId: Int): Invitation {
        return invitationRepository.createInvitation(senderId, receiverId, channelId)
    }

    fun getInvitationDetails(id: Int): Invitation? {
        return invitationRepository.findById(id)
    }

    fun acceptInvitation(id: Int) {
        invitationRepository.acceptInvitation(id)
    }

    fun declineInvitation(id: Int) {
        invitationRepository.deleteById(id)
    }

    fun getInvitationsOfUser(userId: Int): List<Invitation> {
        return invitationRepository.getInvitationsOfUser(userId)
    }

}

