interface SessionRepository{

    fun findByToken(token: String): Session?

    fun findByUserId(userId: Int): Session?

    fun createSession(
        userId: Int,
        channel: Channel,
    ): Session

    fun getSessionHistory(
        userId: Int,
        limit: Int,
        skip: Int,
    ): List<Session>

    fun deleteSession(
        id: Int,
    ): Boolean
}