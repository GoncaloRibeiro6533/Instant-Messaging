class InvitationService(private val invitationService: InvitationService){

    fun createInvitation(sender: User, receiver: User, channel: Channel): Invitation {
        return invitationService.createInvitation(sender, receiver, channel)
    }

    fun getInvitationDetails(id: Int) : Invitation {
        return invitationService.getInvitationDetails(id)
    }

    fun acceptInvitation(id: Int) {
        invitationService.acceptInvitation(id)
    }

}

