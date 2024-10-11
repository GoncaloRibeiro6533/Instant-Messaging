interface UserRepository {
    fun findById(id: Int): User?

    fun findByToken(token: String): User?

    fun findByUsername(
        username: String,
        limit: Int,
        skip: Int,
    ): List<User>

    fun create(
        username: String,
        email: String,
        password: String,
        token: String,
    ): User

    fun updateUsername(
        token: String,
        newUsername: String,
    ): User

    fun getByUsernameAndPassword(
        username: String,
        password: String,
    ): User?

    fun delete(id: Int): User

    @Suppress("RedundantUnitReturnType")
    fun clear(): Unit
}
