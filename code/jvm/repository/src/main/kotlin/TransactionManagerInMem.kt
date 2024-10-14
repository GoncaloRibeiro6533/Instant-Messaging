import jakarta.inject.Named
import mocks.MockChannelRepository
import mocks.MockInvitationRepo
import mocks.MockMessageRepo
import mocks.MockSessionRepository
import mocks.MockUserRepository

@Named
class TransactionManagerInMem : TransactionManager {
    private val channelRepo = MockChannelRepository()
    private val userRepo = MockUserRepository()
    private val messageRepo = MockMessageRepo()
    private val invitationRepo = MockInvitationRepo()
    private val sessionRepo = MockSessionRepository()

    override fun <R> run(block: Transaction.() -> R): R {
        return block(TransactionInMem(channelRepo, userRepo, messageRepo, invitationRepo, sessionRepo))
    }
}
