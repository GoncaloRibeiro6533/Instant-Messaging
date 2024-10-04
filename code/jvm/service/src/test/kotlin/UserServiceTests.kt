import Errors.*
import mocks.MockUserRepository
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class UserServiceTests {

    private val mock = MockUserRepository()
    private val userServices = UserServices(mock)
    @Test
    fun `should return user by id`() {
        val user = MockUserRepository.users[0]
        val result = userServices.getUserById(user.id, user.token)
        assertEquals(user, result)
    }

    @Test
    fun `should return user by username`() {
        val user = MockUserRepository.users[0]
        val result = userServices.findUserByUsername(user.username, user.token)
        assertEquals(listOf(user), result)
    }

    @Test
    fun `register user`() {
        val username = "user2"
        val password = "password2"
        val result = userServices.createUser(username, password)
        assertEquals(username, result.username)
    }
    @Test
    fun `login user`() {
        val user = MockUserRepository.users[0]
        val password = MockUserRepository.passwords[user.id]
        checkNotNull(password)
        val result = userServices.loginUser(user.username, password)
        assertEquals(user, result)
    }

    @Test
    fun `update username`() {
        val user = MockUserRepository.users[0]
        val newUsername = "newUsername"
        val result = userServices.updateUsername(user.token, newUsername)
        assertEquals(newUsername, result.username)
    }

    @Test
    fun testIsValidToken() {
        val user = MockUserRepository.users[0]
        val result = userServices.isValidToken(user.token)
        assertEquals(user, result)
    }

    @Test
    fun `getUserById should throw BadRequestException when id is less than 0`() {
        val user = MockUserRepository.users[0]
        assertFailsWith<BadRequestException> {
            userServices.getUserById(-1, user.token)
        }
    }

    @Test
    fun `getUserById should throw NotFoundException when user is not found`() {
        val user = MockUserRepository.users[0]
        assertFailsWith<NotFoundException> {
            userServices.getUserById(3123, user.token)
        }
    }

    @Test
    fun `findUserByUsername should throw Unauthorized when user is not authenticated`() {
        MockUserRepository.users[0]
        assertFailsWith<UnauthorizedException> {
            userServices.findUserByUsername("user1", "invalidToken")
        }
    }

    @Test
    fun `createUser should throw BadRequestException when username is blank`() {
        assertFailsWith<BadRequestException> {
            userServices.createUser("", "password")
        }
    }

    @Test
    fun `createUser should throw BadRequestException when password is blank`() {
        assertFailsWith<BadRequestException> {
            userServices.createUser("user", "")
        }
    }

    @Test
    fun `createUser should throw BadRequestException when username is greater than 50 characters`() {
        assertFailsWith<BadRequestException> {
            userServices.createUser("a".repeat(User.MAX_USERNAME_LENGTH + 1), "password")
        }
    }

    @Test
    fun `createUser should throw BadRequestException when username already exists`() {
        val user = MockUserRepository.users[0]
        assertFailsWith<BadRequestException> {
            userServices.createUser(user.username, "password")
        }
    }

    @Test
    fun `loginUser should throw BadRequestException when username is blank`() {
        assertFailsWith<BadRequestException> {
            userServices.loginUser("", "password")
        }
    }

    @Test
    fun `loginUser should throw BadRequestException when password is blank`() {
        assertFailsWith<BadRequestException> {
            userServices.loginUser("user", "")
        }
    }

    @Test
    fun `loginUser should throw UnauthorizedException when username is invalid`() {
        assertFailsWith<UnauthorizedException> {
            userServices.loginUser("invalidUser", "password")
        }
    }

    @Test
    fun `loginUser should throw UnauthorizedException when password is invalid`() {
        val user = MockUserRepository.users[0]
        assertFailsWith<UnauthorizedException> {
            userServices.loginUser(user.username, "invalidPassword")
        }
    }

    @Test
    fun `updateUsername should throw BadRequestException when new username is blank`() {
        val user = MockUserRepository.users[0]
        assertFailsWith<BadRequestException> {
            userServices.updateUsername(user.token, "")
        }
    }

    @Test
    fun `updateUsername should throw BadRequestException when new username is greater than 50 characters`() {
        val user = MockUserRepository.users[0]
        assertFailsWith<BadRequestException> {
            userServices.updateUsername(user.token, "a".repeat(User.MAX_USERNAME_LENGTH + 1))
        }
    }

    @Test
    fun `updateUsername should throw BadRequestException when new username already exists`() {
        val user = MockUserRepository.users[0]
        assertFailsWith<BadRequestException> {
            userServices.updateUsername(user.token, user.username)
        }
    }
/*
    @Test
    fun `getUnreadMessages should throw NotFoundException when user is not found`() {
        assertFailsWith<NotFoundException> {
            userServices.getUnreadMessages(1)
        }
    }

    @Test
    fun `getUnreadMessages should return empty map when user has no unread messages`() {
        val user = MockUserRepository.users[0]
        val result = userServices.getUnreadMessages(user.id)
        assertEquals(emptyMap<Channel,List<Message>>(), result)
    }
*/
    /*
    @Test
    fun `getUnreadMessages should return map with unread messages grouped by channel`() {
        val user = MockUserRepository.users[0]
        val channel = Channel(0, "channel", user, Visibility.PUBLIC, emptyList(), mapOf( user to Role.READ_WRITE))
        val timestamp = ZonedDateTime.of(2024, 10, 3, 15, 0, 0, 0, ZoneId.of("Europe/Lisbon"))
        val message = Message(0, user, channel, "message", timestamp.toLocalDateTime())
        val userWithMessages = user.copy(unreadMessages = listOf(message))
        MockUserRepository.users[0] = userWithMessages
        val result = userServices.getUnreadMessages(user.id)
        assertEquals(mapOf(channel to listOf(message)), result)
    }*/
}