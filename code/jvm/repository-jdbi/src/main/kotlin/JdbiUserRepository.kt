import org.jdbi.v3.core.Handle

class JdbiUserRepository(
    private val handle: Handle,
) : UserRepository {
    override fun findById(id: Int): User? {
        return handle.createQuery("SELECT * FROM dbo.User WHERE id = :id")
            .bind("id", id)
            .mapTo(User::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findByUsername(
        username: String,
        limit: Int,
        skip: Int,
    ): List<User> {
        return handle.createQuery("SELECT * FROM dbo.User WHERE username = :username LIMIT :limit OFFSET :skip")
            .bind("username", username)
            .bind("limit", limit)
            .bind("skip", skip)
            .mapTo(User::class.java)
            .list()
    }

    override fun create(
        username: String,
        email: String,
        password: String,
    ): User {
        TODO("Not yet implemented")
    }

    override fun updateUsername(
        userId: Int,
        newUsername: String,
    ): User {
        TODO("Not yet implemented")
    }

    override fun getByUsernameAndPassword(
        username: String,
        password: String,
    ): User? {
        TODO("Not yet implemented")
    }

    override fun delete(id: Int): User {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<User> {
        TODO("Not yet implemented")
    }

    override fun findByEmail(email: String): User? {
        TODO("Not yet implemented")
    }
}
