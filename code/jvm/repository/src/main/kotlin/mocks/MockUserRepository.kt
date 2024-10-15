package mocks

import User
import UserRepository

class MockUserRepository : UserRepository {
    private val users = mutableListOf<User>()
    private val passwordsHash = mutableMapOf<Int, String>()
    private val saltColumn = mutableMapOf<Int, String>()
    private var currentId = 0

    override fun findById(id: Int) = users.firstOrNull { it.id == id }

    override fun findByUsername(
        username: String,
        limit: Int,
        skip: Int,
    ): List<User> =
        users.filter { it.username.trim().uppercase().contains(username.uppercase()) }
            .drop(skip)
            .take(limit)

    override fun create(
        username: String,
        email: String,
        password: String,
    ): User {
        users.add(User(currentId++, username.trim(), email))
        val user = users.last()
        passwordsHash[user.id] = password
        return user
    }

    override fun updateUsername(
        userId: Int,
        newUsername: String,
    ): User {
        val user = users.first { it.id == userId }
        users.remove(user)
        val userEdited = user.copy(username = newUsername)
        users.add(user)
        return userEdited
    }

    override fun getByUsernameAndPassword(
        username: String,
        password: String,
    ): User? {
        val user = users.firstOrNull { it.username == username }
        return if (user != null && passwordsHash[user.id] == password) {
            user
        } else {
            null
        }
    }

    override fun delete(id: Int): User {
        val user = users.first { it.id == id }
        users.remove(user)
        return user
    }

    override fun clear() {
        users.clear()
        passwordsHash.clear()
        currentId = 0
    }

    override fun findAll() = users.toList()

    override fun findByEmail(email: String) = users.firstOrNull { it.email == email }
}
