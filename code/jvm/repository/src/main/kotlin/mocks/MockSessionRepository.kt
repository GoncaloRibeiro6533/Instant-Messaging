package mocks

import Session
import SessionRepository
import kotlinx.datetime.Instant

class MockSessionRepository : SessionRepository {
    private val sessions = mutableListOf<Session>()

    override fun findByToken(token: String): Session? {
        return sessions.firstOrNull { it.token == token }
    }

    override fun findByUserId(userId: Int): List<Session> {
        return sessions.filter { it.userId == userId }
    }

    override fun createSession(
        userId: Int,
        token: Session,
    ): Session {
        val session = Session(token.token, userId, Instant.DISTANT_FUTURE, Instant.DISTANT_PAST)
        sessions.add(session)
        return session
    }

    override fun getSessionHistory(
        userId: Int,
        limit: Int,
        skip: Int,
    ): List<Session> {
        return sessions.filter { it.userId == userId }
            .drop(skip)
            .take(limit)
    }

    override fun deleteSession(token: String): Boolean {
        val session = sessions.firstOrNull { it.token == token }
        return if (session != null) {
            sessions.remove(session)
            true
        } else {
            false
        }
    }

    override fun clear() {
        sessions.clear()
    }
}
