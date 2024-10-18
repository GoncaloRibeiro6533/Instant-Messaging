import pt.isel.Token

interface SessionRepository {
    fun findByToken(token: String): Token?

    fun findByUserId(userId: Int): List<Token>

    fun createSession(
        userId: Int,
        token: Token,
    ): Token

    fun getSessionHistory(
        userId: Int,
        limit: Int,
        skip: Int,
    ): List<Token>

    fun deleteSession(token: String): Boolean

    fun clear(): Unit
}
