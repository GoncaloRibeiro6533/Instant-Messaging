import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.Sha256TokenEncoder
import pt.isel.Token
import pt.isel.TokenValidationInfo
import pt.isel.User
import pt.isel.UsersDomain
import pt.isel.UsersDomainConfig
import pt.isel.mocks.MockSessionRepository
import pt.isel.mocks.MockUserRepository
import kotlin.time.Duration.Companion.hours

class TokenRepoMockTests {
    private var user: User
    private val usersDomainConfig =
        UsersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = 24.hours,
            tokenRollingTtl = 1.hours,
            maxTokensPerUser = 3,
        )
    private val tokenEncoder = Sha256TokenEncoder()
    private val usersDomain =
        UsersDomain(
            tokenEncoder = tokenEncoder,
            passwordEncoder = BCryptPasswordEncoder(),
            config = usersDomainConfig,
        )

    private val repoUsers =
        MockUserRepository().also {
            user =
                it.createUser(
                    "Bob",
                    "bob@mail.com",
                    "password",
                )
        }

    private val repoSessions = MockSessionRepository()

    private val token = Token(TokenValidationInfo("token"), user.id, Clock.System.now(), Clock.System.now())

    @Test
    fun `Test create session`() {
        val s = repoSessions.createSession(user.id, token, usersDomain.maxNumberOfTokensPerUser)
        assertEquals(user.id, s.userId)
    }

    @Test
    fun `Test get session history`() {
        repoSessions.createSession(user.id, token, usersDomain.maxNumberOfTokensPerUser)
        repoSessions.createSession(user.id, token, usersDomain.maxNumberOfTokensPerUser)
        val sessions = repoSessions.getSessionHistory(user.id, 2, 0)
        assertEquals(2, sessions.size)
    }

    @Test
    fun `Test delete session`() {
        repoSessions.createSession(user.id, token, usersDomain.maxNumberOfTokensPerUser)
        val isDeleted = repoSessions.deleteSession("token")
        assertEquals(true, isDeleted)
    }

    @Test
    fun `Test find by token`() {
        val session = repoSessions.createSession(user.id, token, usersDomain.maxNumberOfTokensPerUser)
        val foundSession = repoSessions.findByToken("token")
        assertEquals(session, foundSession)
    }

    @Test
    fun `Test find by user id`() {
        repoSessions.createSession(user.id, token, usersDomain.maxNumberOfTokensPerUser)
        repoSessions.createSession(user.id, token, usersDomain.maxNumberOfTokensPerUser)
        val sessions = repoSessions.findByUserId(user.id)
        assertEquals(2, sessions.size)
    }

    @Test
    fun `Test clear`() {
        repoSessions.createSession(user.id, token, usersDomain.maxNumberOfTokensPerUser)
        repoSessions.clear()
        val sessions = repoSessions.findByUserId(user.id)
        assertEquals(0, sessions.size)
    }

    @Test
    fun `Delete non existing session`() {
        val isDeleted = repoSessions.deleteSession("token")
        assertEquals(false, isDeleted)
    }
}
