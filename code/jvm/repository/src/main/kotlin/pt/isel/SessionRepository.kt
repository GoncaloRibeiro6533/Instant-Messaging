package pt.isel

import kotlinx.datetime.Instant

interface SessionRepository {
    fun findByToken(token: String): Token?

    fun findByUser(user: User): List<Token>

    fun createSession(
        user: User,
        token: Token,
        maxTokens: Int,
    ): Token

    fun getSessionHistory(
        user: User,
        limit: Int,
        skip: Int,
    ): List<Token>

    fun deleteSession(token: Token): Boolean

    fun clear(): Unit

    fun updateSession(
        token: Token,
        lastTimeUsed: Instant,
    ): Token
}
