package pt.isel

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.slf4j.LoggerFactory
import java.sql.ResultSet

class JdbiSessionRepository(
    private val handle: Handle,
) : SessionRepository {
    override fun createSession(
        user: User,
        token: Token,
        maxTokens: Int,
    ): Token {
        val deletions =
            handle
                .createUpdate(
                    """
                    delete from dbo.TOKEN 
                    where user_id = :user_id 
                        and token in (
                            select token from dbo.TOKEN where user_id = :user_id 
                                order by last_used_at desc offset :offset
                        )
                    """.trimIndent(),
                ).bind("user_id", user.id)
                .bind("offset", maxTokens - 1)
                .execute()

        logger.info("{} tokens deleted when creating new token", deletions)

        return handle.createUpdate(
            """ 
            INSERT INTO dbo.token(token, user_id, created_at, last_used_at)
                VALUES (:token, :user_id, :created_at, :last_used_at)
            """.trimIndent(),
        ).bind("token", token.token.validationInfo)
            .bind("user_id", user.id)
            .bind("created_at", token.createdAt.epochSeconds)
            .bind("last_used_at", token.lastUsedAt.epochSeconds)
            .executeAndReturnGeneratedKeys()
            .map(TokenMapper()).one()
    }

    override fun findByToken(token: String): Token? {
        return handle.createQuery(
            "SELECT * FROM dbo.token WHERE token = :token",
        ).bind("token", token)
            .map(TokenMapper())
            .findFirst()
            .orElse(null)
    }

    override fun findByUser(user: User): List<Token> {
        return handle.createQuery(
            "SELECT * FROM dbo.token WHERE user_id = :userId",
        ).bind("userId", user.id)
            .mapTo(Token::class.java)
            .list()
    }

    override fun getSessionHistory(
        user: User,
        limit: Int,
        skip: Int,
    ): List<Token> {
        return handle.createQuery(
            "SELECT * FROM dbo.token WHERE user_id = :userId LIMIT :limit OFFSET :skip",
        ).bind("userId", user.id)
            .bind("limit", limit)
            .bind("skip", skip)
            .mapTo(Token::class.java)
            .list()
    }

    override fun deleteSession(token: Token): Boolean {
        return handle.createUpdate(
            "DELETE FROM dbo.token WHERE token = :token",
        ).bind("token", token.token.validationInfo)
            .execute() > 0
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.token")
            .execute()
    }

    override fun updateSession(
        token: Token,
        lastTimeUsed: Instant,
    ): Token {
        return handle.createUpdate(
            """
            UPDATE dbo.token
            SET last_used_at = :last_used_at
            WHERE token = :token
            """.trimIndent(),
        ).bind("last_used_at", lastTimeUsed.epochSeconds)
            .bind("token", token.token.validationInfo)
            .executeAndReturnGeneratedKeys()
            .map(TokenMapper())
            .one()
    }

    private class TokenMapper : RowMapper<Token> {
        override fun map(
            rs: ResultSet,
            ctx: StatementContext,
        ): Token {
            return Token(
                token = TokenValidationInfo(rs.getString("token")),
                userId = rs.getInt("user_id"),
                createdAt = Instant.fromEpochSeconds(rs.getLong("created_at")),
                lastUsedAt = Instant.fromEpochSeconds(rs.getLong("last_used_at")),
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JdbiSessionRepository::class.java)
    }
}
