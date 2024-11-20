package pt.isel.mocks

import pt.isel.SessionRepository
import pt.isel.Token

class MockSessionRepository : SessionRepository {
    private val tokens = mutableListOf<Token>()

    override fun findByToken(token: String): Token? {
        return tokens.firstOrNull { it.token.validationInfo == token }
    }

    override fun findByUserId(userId: Int): List<Token> {
        return tokens.filter { it.userId == userId }
    }

    override fun createSession(
        userId: Int,
        token: Token,
        maxTokens: Int,
    ): Token {
        tokens.filter { it.userId == userId }
            .sortedByDescending { it.lastUsedAt }
            .drop(maxTokens - 1)
            .forEach { tokens.remove(it) }
        val session = Token(token.token, userId, token.createdAt, token.lastUsedAt)
        tokens.add(session)
        return session
    }

    override fun getSessionHistory(
        userId: Int,
        limit: Int,
        skip: Int,
    ): List<Token> {
        return tokens.filter { it.userId == userId }
            .drop(skip)
            .take(limit)
    }

    override fun deleteSession(token: String): Boolean {
        val session = tokens.firstOrNull { it.token.validationInfo == token }
        return if (session != null) {
            tokens.remove(session)
            true
        } else {
            false
        }
    }

    override fun clear() {
        tokens.clear()
    }
}
