import UserRepository


class TransactionInMem(

    override val channelRepo: ChannelRepository,
    override val userRepo: UserRepository,
    override val messageRepo: MessageRepository,
    override val invitationRepo: InvitationRepository,
) : Transaction {

    override fun rollback() {
        throw UnsupportedOperationException()
    }
}