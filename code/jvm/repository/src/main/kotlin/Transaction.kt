

interface Transaction {
    val channelRepo: ChannelRepository
    val userRepo: UserRepository
    val messageRepo: MessageRepository
    val invitationRepo: InvitationRepository
    val sessionRepo: SessionRepository

    fun rollback()
}
