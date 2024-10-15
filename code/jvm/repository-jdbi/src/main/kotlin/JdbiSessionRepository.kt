import org.jdbi.v3.core.Handle

class JdbiSessionRepository(
    private val handle: Handle,
) : SessionRepository {
    override fun findByToken(token: String): Session? {
        TODO("Not yet implemented")
    }

    override fun findByUserId(userId: Int): List<Session> {
        TODO("Not yet implemented")
    }

    override fun createSession(
        userId: Int,
        token: String,
    ): Session {
        TODO("Not yet implemented")
    }

    override fun getSessionHistory(
        userId: Int,
        limit: Int,
        skip: Int,
    ): List<Session> {
        TODO("Not yet implemented")
    }

    override fun deleteSession(token: String): Boolean {
        TODO("Not yet implemented")
    }
}
