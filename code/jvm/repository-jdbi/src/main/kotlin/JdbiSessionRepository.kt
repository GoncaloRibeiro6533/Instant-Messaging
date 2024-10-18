import org.jdbi.v3.core.Handle
import pt.isel.Token

class JdbiSessionRepository(
    private val handle: Handle,
) : SessionRepository {
    override fun findByToken(token: String): Token? {
        return handle.createQuery(
            "SELECT * FROM dbo.session WHERE token = :token",
        ).bind("token", token)
            .mapTo(Token::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findByUserId(userId: Int): List<Token> {
        return handle.createQuery(
            "SELECT * FROM dbo.session WHERE user_id = :userId",
        ).bind("userId", userId)
            .mapTo(Token::class.java)
            .list()
    }

    override fun createSession(
        userId: Int,
        token: Token,
    ): Token {
        return handle.createUpdate(
            "INSERT INTO dbo.session(token, user_id, created_at, last_used_at) " +
                "VALUES (:token, :user_id, :created_at, :last_used_at)",
        ).bind("token", token)
            .bind("user_id", userId)
            .bind("created_at", token.createdAt.epochSeconds)
            .bind("last_used_at", token.lastUsedAt.epochSeconds)
            .executeAndReturnGeneratedKeys()
            .mapTo(Token::class.java)
            .one()
    }

    override fun getSessionHistory(
        userId: Int,
        limit: Int,
        skip: Int,
    ): List<Token> {
        return handle.createQuery(
            "SELECT * FROM dbo.session WHERE user_id = :userId LIMIT :limit OFFSET :skip",
        ).bind("userId", userId)
            .bind("limit", limit)
            .bind("skip", skip)
            .mapTo(Token::class.java)
            .list()
    }

    override fun deleteSession(token: String): Boolean {
        return handle.createUpdate(
            "DELETE FROM dbo.session WHERE token = :token",
        ).bind("token", token)
            .execute() > 0
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.session")
            .execute()
    }
}
