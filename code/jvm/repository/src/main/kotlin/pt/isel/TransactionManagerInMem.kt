package pt.isel

import jakarta.inject.Named
import pt.isel.mocks.MockChannelRepository
import pt.isel.mocks.MockInvitationRepo
import pt.isel.mocks.MockMessageRepo
import pt.isel.mocks.MockSessionRepository
import pt.isel.mocks.MockUserRepository

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
