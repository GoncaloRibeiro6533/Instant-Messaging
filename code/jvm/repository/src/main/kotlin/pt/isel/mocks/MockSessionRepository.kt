package pt.isel.mocks

import kotlinx.datetime.Instant
import pt.isel.SessionRepository
import pt.isel.Token
import pt.isel.User

class MockSessionRepository : SessionRepository {
    private val tokens = mutableListOf<Token>()

    override fun findByToken(token: String): Token? {
        return tokens.firstOrNull { it.token.validationInfo == token }
    }

    override fun findByUser(user: User): List<Token> {
        return tokens.filter { it.userId == user.id }
    }

    override fun createSession(
        user: User,
        token: Token,
        maxTokens: Int,
    ): Token {
        tokens.filter { it.userId == user.id }
            .sortedByDescending { it.lastUsedAt }
            .drop(maxTokens - 1)
            .forEach { tokens.remove(it) }
        val session = Token(token.token, user.id, token.createdAt, token.lastUsedAt)
        tokens.add(session)
        return session
    }

    override fun getSessionHistory(
        user: User,
        limit: Int,
        skip: Int,
    ): List<Token> {
        return tokens.filter { it.userId == user.id }
            .drop(skip)
            .take(limit)
    }

    override fun deleteSession(token: Token): Boolean {
        return tokens.remove(token)
    }

    override fun clear() {
        tokens.clear()
    }

    override fun updateSession(
        token: Token,
        lastTimeUsed: Instant,
    ): Token {
        val session = tokens.first { it.token == token.token }
        val updatedSession = Token(token.token, token.userId, token.createdAt, lastTimeUsed)
        tokens.remove(session)
        tokens.add(updatedSession)
        return updatedSession
    }
}
