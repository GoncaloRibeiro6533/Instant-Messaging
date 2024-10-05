import mocks.MockUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertIs

class UserServiceTests {

    private lateinit var mock: MockUserRepository
    private lateinit var userServices : UserService

    @BeforeEach
    fun setUp() {
        mock = MockUserRepository()
        mock.clear()
        userServices = UserService(mock)
    }

    @Test
    fun `register user`() {
        val username = "user2"
        val password = "password2"
        val result = userServices.createUser(username, password)
        assertIs<Success<User>>(result)
        assertEquals(username, result.value.username)
    }
    @Test
    fun `login user`() {
        val username = "Bob"
        val password = "password"
        val user = userServices.createUser(username, password)
        assertIs<Success<User>>(user)
        val result = userServices.loginUser(username, password)
        assertIs<Success<User>>(result)
        assertEquals(user.value, result.value)
    }


    @Test
    fun `should return user by id`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val result = userServices.getUserById(user.value.id, user.value.token)
        assertIs<Success<User>>(result)
        assertEquals(user.value, result.value)
    }

    @Test
    fun `should return Unauthorized if token is not valid`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val result = userServices.getUserById(user.value.id, "invalidToken")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.Unauthorized, result.value)
    }
    @Test
    fun `should return user by username`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val result = userServices.findUserByUsername("Bob", user.value.token)
        assertIs<Success<User>>(result)
        assertEquals(listOf(user.value), result.value)
    }


    @Test
    fun `update username`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val newUsername = "newUsername"
        val result = userServices.updateUsername(user.value.token, newUsername)
        assertIs<Success<User>>(result)
        assertEquals(newUsername, result.value.username)
    }

    @Test
    fun `getUserById should get Failure with NegativeIdentifier when id is less than 0`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val result = userServices.getUserById(-1, user.value.token)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NegativeIdentifier, result.value)
    }

    @Test
    fun `getUserById should return UserNotFound when user is not found`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val result = userServices.getUserById(100, user.value.token)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UserNotFound, result.value)
    }

    @Test
    fun `findUserByUsername should return Unauthorized when user is not authenticated`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val result = userServices.findUserByUsername(user.value.username, "invalidToken")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.Unauthorized, result.value)
    }

    @Test
    fun `createUser should return InvalidUsername when username is blank`() {
        val result = userServices.createUser("", "password")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidUsername, result.value)
    }

    @Test
    fun `createUser should return InvalidPassword when password is blank`() {
        val result = userServices.createUser("username", "")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidPassword, result.value)
    }

    @Test
    fun `createUser should return UsernameToLong when username is greater than 50 characters`() {
        val result = userServices.createUser("a".repeat(User.MAX_USERNAME_LENGTH + 1), "password")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameToLong, result.value)
    }

    @Test
    fun `createUser should return UsernameAlreadyExists when username already exists`() {
        userServices.createUser("Bob", "password")
        val result = userServices.createUser("Bob", "password")
        assertIs<Failure<UserError>>(result)
    }

    @Test
    fun `loginUser should  return  when username is blank`() {
        val result = userServices.loginUser("", "password")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidUsername, result.value)
    }

    @Test
    fun `loginUser should return InvalidPassword when password is blank`() {
        val result = userServices.loginUser("username", "")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidPassword, result.value)
    }

    @Test
    fun `loginUser should return NoMatchingUsername when username is invalid`() {
        val result = userServices.loginUser("invalidUsername", "password")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NoMatchingUsername, result.value)
    }

    @Test
    fun `loginUser should return NoMatchingPassword when password is invalid`() {
        userServices.createUser("Bob", "password")
        val result = userServices.loginUser("Bob", "invalidPassword")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NoMatchingPassword, result.value)
    }

    @Test
    fun `updateUsername should return InvalidUsername when new username is blank`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val result = userServices.updateUsername(user.value.token, "")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidUsername, result.value)
    }

    @Test
    fun `updateUsername should return UsernameToLong when new username is greater than 50 characters`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val result = userServices
            .updateUsername(user.value.token, "a".repeat(User.MAX_USERNAME_LENGTH + 1))
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameToLong, result.value)
    }

    @Test
    fun `updateUsername should return UsernameAlreadyExists when new username already exists`() {
        val user = userServices.createUser("Bob", "password")
        userServices.createUser("Bob2", "password")
        assertIs<Success<User>>(user)
        val result = userServices.updateUsername(user.value.token, "Bob2")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameAlreadyExists, result.value)
    }

    @Test
    fun `updateUsername should return Unauthorized when user is not authenticated`() {
        val user = userServices.createUser("Bob", "password")
        assertIs<Success<User>>(user)
        val result = userServices.updateUsername("invalidToken", "newUsername")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.Unauthorized, result.value)
    }
}