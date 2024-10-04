
class InvitationService(private val invitationRepository: InvitationRepository){

    fun createInvitation(sender: User, receiver: User, channel: Channel): Invitation {
        return invitationRepository.createInvitation(sender, receiver, channel)
    }

    fun getInvitationDetails(id: Int) : Invitation {
        return invitationRepository.getInvitationDetails(id)
    }

    fun acceptInvitation(id: Int) {
        invitationRepository.acceptInvitation(id)
    }

}

