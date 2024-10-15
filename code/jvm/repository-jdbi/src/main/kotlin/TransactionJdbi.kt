import org.jdbi.v3.core.Handle

class TransactionJdbi(
    private val handle: Handle
) :Transaction {
    override val channelRepo = JdbiChannelRepository(handle)
    override val userRepo = JdbiUserRepository(handle)
    override val messageRepo = JdbiMessageRepository(handle)
    override val invitationRepo = JdbiInvitationRepository(handle)
    override val sessionRepo = JdbiSessionRepository(handle)

    override fun rollback() {
        handle.rollback()
    }

}