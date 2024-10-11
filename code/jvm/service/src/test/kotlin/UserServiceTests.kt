import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class UserServiceTests {
    private lateinit var userService: UserService
    private lateinit var invitationService: InvitationService
    private lateinit var channelService: ChannelService

    @BeforeEach
    fun setUp() {
        val trxManager = TransactionManagerInMem()
        userService = UserService(trxManager)
        invitationService = InvitationService(trxManager)
        channelService = ChannelService(trxManager)
    }

    @Test
    fun `register first user should succeed and return user`() {
        val result =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(result)
        assertEquals("admin", result.value.username)
    }

    @Test
    fun `register first user should return NotFirstUser when user already exists`() {
        val firstUser =
            userService.addFirstUser("admin", "password", "admin@mail.com")
        assertIs<Success<User>>(firstUser)
        val secondUser =
            userService.addFirstUser("Bob", "password", "bob@mail.com")
        assertIs<Failure<UserError>>(secondUser)
        assertEquals(UserError.NotFirstUser, secondUser.value)
    }

    @Test
    fun `register user should succeed and return user with invitation with no channel`() {
        val user = userService.addFirstUser("admin", "password", "admin@mail.com")
        assertIs<Success<User>>(user)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "bob@mail.com",
                null,
                null,
                user.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val username = "Bob"
        val result =
            userService.createUser(username, registerInvitation.value.email, "password", registerInvitation.value.id)
        assertIs<Success<User>>(result)
        assertEquals(username, result.value.username)
    }

    @Test
    fun `login user should succed and return user`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val result = userService.loginUser(admin.value.username, "password")
        assertIs<Success<User>>(result)
        assertEquals(admin.value, result.value)
    }

    @Test
    fun `should return user by id`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val result = userService.getUserById(admin.value.id, admin.value.token)
        assertIs<Success<User>>(result)
        assertEquals(admin.value, result.value)
    }

    @Test
    fun `should return Unauthorized if token is not valid`() {
        val user =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(user)
        val result = userService.getUserById(user.value.id, "invalidToken")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.Unauthorized, result.value)
    }

    @Test
    fun `should return user by username`() {
        val user =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(user)
        val result = userService.findUserByUsername("admin", user.value.token)
        assertIs<Success<List<User>>>(result)
        assertEquals(listOf(user.value), result.value)
    }

    @Test
    fun `should update username and return user`() {
        val user =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(user)
        val newUsername = "newUsername"
        val result = userService.updateUsername(user.value.token, newUsername)
        assertIs<Success<User>>(result)
        assertEquals(newUsername, result.value.username)
    }

    @Test
    fun `getUserById should get Failure with NegativeIdentifier when id is less than 0`() {
        val user =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(user)
        val result = userService.getUserById(-1, user.value.token)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NegativeIdentifier, result.value)
    }

    @Test
    fun `getUserById should return UserNotFound when user is not found`() {
        val user =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(user)
        val result = userService.getUserById(100, user.value.token)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UserNotFound, result.value)
    }

    @Test
    fun `findUserByUsername should return Unauthorized when user is not authenticated`() {
        val user =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(user)
        val result = userService.findUserByUsername(user.value.username, "invalidToken")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.Unauthorized, result.value)
    }

    @Test
    fun `createUser should return InvalidUsername when username is blank`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                null,
                null,
                admin.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val result =
            userService.createUser(
                "",
                "bob@mail.com",
                "password",
                registerInvitation.value.id,
            )
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidUsername, result.value)
    }

    @Test
    fun `createUser should return InvalidPassword when password is blank`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                null,
                null,
                admin.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val result =
            userService.createUser(
                "username",
                "user1@mail.com",
                "",
                registerInvitation.value.id,
            )
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidPassword, result.value)
    }

    @Test
    fun `createUser should return UsernameToLong when username is greater than 50 characters`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                null,
                null,
                admin.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val result =
            userService
                .createUser(
                    "a".repeat(User.MAX_USERNAME_LENGTH + 1),
                    "bob@mail.com",
                    "password",
                    registerInvitation.value.id,
                )
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameToLong, result.value)
    }

    @Test
    fun `createUser should return UsernameAlreadyExists when username already exists`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                null,
                null,
                admin.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val result =
            userService.createUser(
                "admin",
                "bob@mail.com",
                "password",
                registerInvitation.value.id,
            )
        assertIs<Failure<UserError>>(result)
    }

    @Test
    fun `loginUser should  return  when username is blank`() {
        val result = userService.loginUser("", "password")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidUsername, result.value)
    }

    @Test
    fun `loginUser should return InvalidPassword when password is blank`() {
        val result = userService.loginUser("username", "")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidPassword, result.value)
    }

    @Test
    fun `loginUser should return NoMatchingUsername when username is invalid`() {
        val result = userService.loginUser("invalidUsername", "password")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NoMatchingUsername, result.value)
    }

    @Test
    fun `loginUser should return NoMatchingPassword when password is invalid`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val result = userService.loginUser("admin", "invalidPassword")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NoMatchingPassword, result.value)
    }

    @Test
    fun `updateUsername should return InvalidUsername when new username is blank`() {
        val user =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(user)
        val result = userService.updateUsername(user.value.token, "")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidUsername, result.value)
    }

    @Test
    fun `updateUsername should return UsernameToLong when new username is greater than 50 characters`() {
        val user =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(user)
        val result =
            userService
                .updateUsername(user.value.token, "a".repeat(User.MAX_USERNAME_LENGTH + 1))
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameToLong, result.value)
    }

    @Test
    fun `updateUsername should return UsernameAlreadyExists when new username already exists`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                null,
                null,
                admin.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val newUser = userService.createUser("Bob", "bob@mail.com", "password", registerInvitation.value.id)
        assertIs<Success<User>>(newUser)
        val result = userService.updateUsername(newUser.value.token, "admin")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameAlreadyExists, result.value)
    }

    @Test
    fun `updateUsername should return Unauthorized when user is not authenticated`() {
        val result = userService.updateUsername("invalidToken", "newUsername")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.Unauthorized, result.value)
    }

    @Test
    fun `deleteUser should return Unauthorized when user is not authenticated`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                null,
                null,
                admin.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val user =
            userService.createUser("Bob", "bob@mail.com", "password", registerInvitation.value.id)
        assertIs<Success<User>>(user)
        val result = userService.deleteUser(user.value.id, "invalidToken")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.Unauthorized, result.value)
    }

    @Test
    fun `deleteUser should return Unauthorized if token is not valid`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val result = userService.deleteUser(admin.value.id, "invalidToken")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.Unauthorized, result.value)
    }

    @Test
    fun `deleteUser should return NegativeIdentifier if id is less than 0`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val result = userService.deleteUser(-1, admin.value.token)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NegativeIdentifier, result.value)
    }

    @Test
    fun `deleteUser should delete user when valid id and token are provided`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val result = userService.deleteUser(admin.value.id, admin.value.token)
        assertIs<Success<User>>(result)
        assertEquals(admin.value, result.value)
    }

    @Test
    fun `clear should remove all users`() {
        val admin =
            userService.addFirstUser(
                "Bob123",
                "password",
                "admin@mail.com",
            )
        assertIs<Success<User>>(admin)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                null,
                null,
                admin.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val user =
            userService.createUser("Bob", "bob@mail.com", "password", registerInvitation.value.id)
        userService.clear()
        val result = userService.findUserByUsername("Bob", "token")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.Unauthorized, result.value)
    }
}
