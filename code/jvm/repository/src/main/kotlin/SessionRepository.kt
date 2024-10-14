interface SessionRepository {
    fun findByToken(token: String): Session?

    fun findByUserId(userId: Int): List<Session>

    fun createSession(
        userId: Int,
        token: String,
    ): Session

    fun getSessionHistory(
        userId: Int,
        limit: Int,
        skip: Int,
    ): List<Session>

    fun deleteSession(token: String): Boolean
}
