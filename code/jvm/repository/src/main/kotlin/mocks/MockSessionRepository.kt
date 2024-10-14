package mocks

import Channel
import Session
import SessionRepository
import User
import java.time.LocalDateTime

class MockSessionRepository : SessionRepository{

    private val sessions = mutableListOf<Session>()

    override fun findByToken(token: String): Session? {
        return sessions.firstOrNull { it.token == token }
    }

    override fun findByUserId(userId: Int): Session? {
        return sessions.firstOrNull { it.userId == userId }
    }

    override fun createSession(
        userId: Int,
        channel: Channel,
    ): Session {
        val session = Session(userId, "token", LocalDateTime.now().plusDays(7))
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

    override fun deleteSession(
        id: Int,
    ): Boolean {
        val session = sessions.firstOrNull { it.userId == id }
        return if (session != null) {
            sessions.remove(session)
            true
        } else {
            false
        }
    }

}

