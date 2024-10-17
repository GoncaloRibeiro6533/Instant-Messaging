import mocks.MockSessionRepository
import mocks.MockUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SessionRepoMockTests {
    private var user: User
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

    @Test
    fun `Test create session`() {
        val session = repoSessions.createSession(user.id, "token")
        assertEquals(user.id, session.userId)
        assertEquals("token", session.token)
    }

    @Test
    fun `Test get session history`() {
        repoSessions.createSession(user.id, "token")
        repoSessions.createSession(user.id, "token")
        val sessions = repoSessions.getSessionHistory(user.id, 2, 0)
        assertEquals(2, sessions.size)
    }

    @Test
    fun `Test delete session`() {
        repoSessions.createSession(user.id, "token")
        val isDeleted = repoSessions.deleteSession("token")
        assertEquals(true, isDeleted)
    }

    @Test
    fun `Test find by token`() {
        val session = repoSessions.createSession(user.id, "token")
        val foundSession = repoSessions.findByToken("token")
        assertEquals(session, foundSession)
    }

    @Test
    fun `Test find by user id`() {
        repoSessions.createSession(user.id, "token")
        repoSessions.createSession(user.id, "token")
        val sessions = repoSessions.findByUserId(user.id)
        assertEquals(2, sessions.size)
    }
}
