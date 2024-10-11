

interface Transaction {
    val channelRepo: ChannelRepository
    val userRepo: UserRepository
    val messageRepo: MessageRepository
    val invitationRepo: InvitationRepository

    fun rollback()
}
