import org.jdbi.v3.core.Handle

class JdbiSessionRepository(
    private val handle: Handle,
) : SessionRepository {
    override fun findByToken(token: String): Session? {
        return handle.createQuery("SELECT * FROM session WHERE token = :token",
        ).bind("token", token)
            .mapTo(Session::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findByUserId(userId: Int): List<Session> {
        return handle.createQuery("SELECT * FROM session WHERE user_id = :userId",
        ).bind("userId", userId)
            .mapTo(Session::class.java)
            .list()
    }

    override fun createSession(
        userId: Int,
        token: String,
    ): Session {
        return handle.createUpdate(
            "INSERT INTO session(user_id, token) VALUES (:userId, :token)",
        ).bind("userId", userId)
            .bind("token", token)
            .executeAndReturnGeneratedKeys()
            .mapTo(Session::class.java)
            .one()
    }

    override fun getSessionHistory(
        userId: Int,
        limit: Int,
        skip: Int,
    ): List<Session> {
        return handle.createQuery(
            "SELECT * FROM session WHERE user_id = :userId LIMIT :limit OFFSET :skip",
        ).bind("userId", userId)
            .bind("limit", limit)
            .bind("skip", skip)
            .mapTo(Session::class.java)
            .list()
    }

    override fun deleteSession(token: String): Boolean {
        return handle.createUpdate("DELETE FROM session WHERE token = :token",
        ).bind("token", token)
            .execute() > 0
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.session")
            .execute()
    }
}
