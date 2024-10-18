package pt.isel

interface UserRepository {
    fun findById(id: Int): User?

    fun findByUsername(
        username: String,
        limit: Int,
        skip: Int,
    ): List<User>

    fun createUser(
        username: String,
        email: String,
        password: String,
    ): User

    fun updateUsername(
        user: User,
        newUsername: String,
    ): User

    fun findByUsernameAndPassword(
        username: String,
        password: String,
    ): User?

    fun delete(id: Int)

    @Suppress("RedundantUnitReturnType")
    fun clear(): Unit

    fun findAll(): List<User>

    fun findByEmail(email: String): User?

    fun findPasswordOfUser(user: User): String
}
