package mocks

import Channel
import Invitation
import User
import UserRepository


class MockUserRepository : UserRepository {

    companion object {
        val users = mutableListOf<User>(
            User(0, "user0", "tokenuser0", emptyList<Channel>(), emptyList<Invitation>()),
        )

        val passwordsHashed = mutableMapOf<Int, String>(
            0 to "0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e"
        )
        val passwords = mutableMapOf<Int, String>(
            0 to "password1"
        )

        var currentId = 0
    }

    override fun findUserById(id: Int) = users.firstOrNull { it.id == id }

    override fun findUserByToken(token: String) = users.firstOrNull { it.token == token }
    override fun findUserByUsername(username: String, limit: Int, skip: Int): List<User> =
        users.filter { it.username.trim().uppercase().contains(username.uppercase()) }
            .drop(skip)
            .take(limit)

    override fun createUser(username: String, password: String): User {
        users.add(User(currentId++, username.trim(), "token"))
        return users.last()
    }
    override fun updateUsername(token: String, newUsername: String): User {
        val user = users.first { it.token == token }
        users.remove(user)
        val userEdited = user.copy(username = newUsername)
        users.add(user)
        return userEdited
    }

    override fun validateLogin(username: String, password: String): User? {
        val user = users.firstOrNull { it.username == username }
        return if (user != null && passwordsHashed[user.id] == password) {
            user
        } else null
    }

    override fun markMessageAsRead(userId: Int, messageId: Int): User {
        TODO("Not yet implemented")
    }
}