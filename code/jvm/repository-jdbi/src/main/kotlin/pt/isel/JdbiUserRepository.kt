package pt.isel

import org.jdbi.v3.core.Handle

class JdbiUserRepository(
    private val handle: Handle,
) : UserRepository {
    override fun findById(id: Int): User? {
        return handle.createQuery("SELECT id, email, username FROM dbo.User WHERE id = :id")
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

    override fun createUser(
        username: String,
        email: String,
        password: String,
    ): User {
        return handle.createUpdate("INSERT INTO dbo.User (username, email, password) VALUES (:username, :email, :password)")
            .bind("username", username)
            .bind("email", email)
            .bind("password", password)
            .executeAndReturnGeneratedKeys()
            .mapTo(User::class.java)
            .one()
    }

    override fun updateUsername(
        user: User,
        newUsername: String,
    ): User {
        handle.createUpdate(
            """
            UPDATE dbo.User set username = :newUsername WHERE id = :id
            """.trimIndent(),
        ).bind("newUsername", newUsername)
            .bind("id", user.id)
            .execute()
        return user.copy(username = newUsername)
    }

    override fun findByUsernameAndPassword(
        username: String,
        password: String,
    ): User? {
        return handle.createQuery("SELECT * FROM dbo.User WHERE username = :username AND password = :password")
            .bind("username", username)
            .bind("password", password)
            .mapTo(User::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun delete(id: Int) {
        handle.createUpdate("DELETE FROM dbo.User WHERE id = :id")
            .bind("id", id)
            .execute()
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.User")
            .execute()
    }

    override fun findAll(): List<User> {
        return handle.createQuery("SELECT * FROM dbo.User")
            .mapTo(User::class.java)
            .list()
    }

    override fun findByEmail(email: String): User? {
        return handle.createQuery("SELECT * FROM dbo.User WHERE email = :email")
            .bind("email", email)
            .mapTo(User::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findPasswordOfUser(user: User): String {
        return handle.createQuery("SELECT password FROM dbo.User WHERE id = :id")
            .bind("id", user.id)
            .mapTo(String::class.java)
            .one()
    }
}
