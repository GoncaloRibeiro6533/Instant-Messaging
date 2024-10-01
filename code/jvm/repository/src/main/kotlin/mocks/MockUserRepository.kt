package mocks

import UsersRepository
import pt.isel.talkRooms.domain.Channel
import pt.isel.talkRooms.domain.Invitation
import pt.isel.talkRooms.domain.User

class UserRepositoryMock : UsersRepository {

    companion object {
        val users = listOf<User>(
            User(1, "user1)", "token1", emptyList<Channel>(), emptyList<Invitation>()),
        )
        var currentId = 0
    }

    override fun findUserById(id: Int) = users.first { it.id == id }

    override fun findUserByUsername(username: String): List<User> = users.filter { it.username.contains(username) }

    override fun createUser(username: String, password: String) = User(++currentId, username, "token", emptyList<Channel>(), emptyList<Invitation>())
}