package pt.isel

interface Emitter {


    fun shutdown()

    fun addEmitter(
        listener: UpdateEmitter,
        authenticatedUser: AuthenticatedUser,
    ): UpdateEmitter

    fun sendEventOfNewMember(
        channel: Channel,
        userToAddInfo: User,
        role: Role,
        users: Set<User>,
    )

    fun sendEventOfNewMessage(
        message: Message,
        users: Set<User>,
    )

    fun sendEventOfChannelNameUpdated(
        updatedChannel: Channel,
        users: Set<User>,
    )

    fun sendEventOfMemberExited(
        channel: Channel,
        user: User,
        users: Set<User>,
    )

    fun sendEventOfNewInvitation(invitation: ChannelInvitation)

    fun sendEventOfInvitationAccepted(invitation: ChannelInvitation)


    fun sendEventOfUsernameUpdate(users: Set<User>, newMember: User)
}