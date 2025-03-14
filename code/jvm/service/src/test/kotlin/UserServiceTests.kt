@file:Suppress("ktlint")
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class UserServiceTests {
    private lateinit var userService: UserService
    private lateinit var invitationService: InvitationService
    private lateinit var channelService: ChannelService

    private val testClock = TestClock()

    private fun createUserService(
        trxManager: TransactionManager,
        testClock: TestClock,
        tokenTtl: Duration = 30.days,
        tokenRollingTtl: Duration = 30.minutes,
        maxTokensPerUser: Int = 3,
    ) = UserService(
        trxManager,
        UsersDomain(
            BCryptPasswordEncoder(),
            Sha256TokenEncoder(),
            UsersDomainConfig(
                tokenSizeInBytes = 256 / 8,
                tokenTtl = tokenTtl,
                tokenRollingTtl,
                maxTokensPerUser = maxTokensPerUser,
            ),
        ),
        testClock,
    )

    private fun createInvitationService(
        trxManager: TransactionManager,
        tokenTtl: Duration = 30.days,
        tokenRollingTtl: Duration = 30.minutes,
        maxTokensPerUser: Int = 3,
        emitter: UpdatesEmitter
    ) = InvitationService(
        trxManager,
        UsersDomain(
            BCryptPasswordEncoder(),
            Sha256TokenEncoder(),
            UsersDomainConfig(
                tokenSizeInBytes = 256 / 8,
                tokenTtl = tokenTtl,
                tokenRollingTtl,
                maxTokensPerUser = maxTokensPerUser,
            ),
        ),
        emitter,
        EmailServiceMock(),
    )

    @BeforeEach
    fun setUp() {
        val trxManager = TransactionManagerInMem()
        val emitter = UpdatesEmitter(trxManager)
        userService = createUserService(trxManager, testClock)
        invitationService = createInvitationService(trxManager, emitter= emitter)
        channelService = ChannelService(trxManager, emitter)
    }

    @Test
    fun `register first user should return UsernameCannotBeBlank when username is blank`() {
        val result =
            userService.addFirstUser(
                "",
                "mailnotvalid",
                "Strong_Password123",
            )
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameCannotBeBlank, result.value)
    }

    @Test
    fun `get user by username with negative skip`() {
        val result = userService.findUserByUsername("admin", 1, -1)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NegativeSkip, result.value)
    }

    @Test
    fun `get user by username with negative limit`() {
        val result = userService.findUserByUsername("admin", -1, 1)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NegativeLimit, result.value)
    }

    @Test
    fun `try to register with already used invitation`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val channel = channelService.createChannel("channel", admin.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "alice@mail.com",
                channel.value.id,
                Role.READ_ONLY,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val newUser =
            userService.createUser(
                "Alice",
                "alice@mail.com",
                "Strong_Password123",
                registerInvitation.value.code,
            )
        assertIs<Success<User>>(newUser)
        val newUser2 =
            userService.createUser(
                "Alice2",
                "alice@mail.com",
                "Strong_Password123",
                registerInvitation.value.code,
            )
        assertIs<Failure<UserError.InvitationAlreadyUsed>>(newUser2)
    }

    @Test
    fun `register first user should return PasswordCannotBeBlank when password is blank`() {
        val result =
            userService.addFirstUser(
                "admin",
                "mailnotvalid",
                "",
            )
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.PasswordCannotBeBlank, result.value)
    }

    @Test
    fun `register first user should succeed and return user`() {
        val result =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(result)
        assertEquals("admin", result.value.username)
    }

    @Test
    fun `register first user should return NotFirstUser when first user already exists`() {
        val firstUser =
            userService.addFirstUser("admin", "admin@mail.com", "Strong_Password123")
        assertIs<Success<User>>(firstUser)
        val secondUser =
            userService.addFirstUser("Bob", "bob@mail.com", "Strong_Password123")
        assertIs<Failure<UserError>>(secondUser)
        assertEquals(UserError.NotFirstUser, secondUser.value)
    }

    @Test
    fun `login user should succeed and return user`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val result = userService.loginUser(admin.value.username, "Strong_Password123")
        assertIs<Success<Cookie>>(result)
        assertEquals(admin.value, result.value.user)
    }

    @Test
    fun `should return user by id`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val result = userService.getUserById(admin.value.id)
        assertIs<Success<User>>(result)
        assertEquals(admin.value, result.value)
    }

    @Test
    fun `should return user by username`() {
        val user =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val result = userService.findUserByUsername("admin")
        assertIs<Success<List<User>>>(result)
        assertEquals(listOf(user.value), result.value)
    }

    @Test
    fun `should update username and return user`() {
        val user =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val newUsername = "newUsername"
        val result = userService.updateUsername(user.value.id, newUsername)
        assertIs<Success<User>>(result)
        assertEquals(newUsername, result.value.username)
    }

    @Test
    fun `getUserById should get Failure with NegativeIdentifier when id is less than 0`() {
        val user =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val result = userService.getUserById(-1)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NegativeIdentifier, result.value)
    }

    @Test
    fun `getUserById should return UserNotFound when user is not found`() {
        val user =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val result = userService.getUserById(100)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UserNotFound, result.value)
    }

    @Test
    fun `createUser should return UsernameCannotBeBlank when username is blank`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val channel = channelService.createChannel("channel", admin.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                channel.value.id,
                Role.READ_ONLY,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val result =
            userService.createUser(
                "",
                "bob@mail.com",
                "Strong_Password123",
                registerInvitation.value.code,
            )
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameCannotBeBlank, result.value)
    }

    @Test
    fun `createUser should return PasswordCannotBeBlank when password is blank`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val channel = channelService.createChannel("channel", admin.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                channel.value.id,
                Role.READ_ONLY,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val result =
            userService.createUser(
                "username",
                "user1@mail.com",
                "",
                registerInvitation.value.code,
            )
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.PasswordCannotBeBlank, result.value)
    }

    @Test
    fun `createUser should return UsernameToLong when username is greater than 50 characters`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val channel = channelService.createChannel("channel", admin.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                channel.value.id,
                Role.READ_ONLY,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val result =
            userService
                .createUser(
                    "a".repeat(User.MAX_USERNAME_LENGTH + 1),
                    "bob@mail.com",
                    "Strong_Password123",
                    registerInvitation.value.code,
                )
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameToLong, result.value)
    }

    @Test
    fun `createUser should return UsernameAlreadyExists when username already exists`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val channel = channelService.createChannel("channel", admin.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val result =
            userService.createUser(
                "admin",
                "bob@mail.com",
                "Strong_Password123",
                registerInvitation.value.code,
            )
        assertIs<Failure<UserError>>(result)
    }

    @Test
    fun `loginUser should  return  when username is blank`() {
        val result = userService.loginUser("", "Strong_Password123")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameCannotBeBlank, result.value)
    }

    @Test
    fun `loginUser should return PasswordCannotBeBlank when password is blank`() {
        val result = userService.loginUser("username", "")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.PasswordCannotBeBlank, result.value)
    }

    @Test
    fun `loginUser should return NoMatchingUsername when username is invalid`() {
        val result = userService.loginUser("invalidUsername", "Strong_Password123")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NoMatchingUsername, result.value)
    }

    @Test
    fun `loginUser should return NoMatchingPassword when password is invalid`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val result = userService.loginUser("admin", "invalidPassword")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.NoMatchingPassword, result.value)
    }

    @Test
    fun `updateUsername should return UsernameCannotBeBlank when new username is blank`() {
        val user =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val result = userService.updateUsername(user.value.id, "")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameCannotBeBlank, result.value)
    }

    @Test
    fun `updateUsername should return UsernameToLong when new username is greater than 50 characters`() {
        val user =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val result =
            userService
                .updateUsername(user.value.id, "a".repeat(User.MAX_USERNAME_LENGTH + 1))
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameToLong, result.value)
    }

    @Test
    fun `updateUsername should return UsernameAlreadyExists when new username already exists`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val channel = channelService.createChannel("channel", admin.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                admin.value.id,
                "bob@mail.com",
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val newUser = userService.createUser("Bob", "bob@mail.com", "Strong_Password123", registerInvitation.value.code)
        assertIs<Success<User>>(newUser)
        val logged2 = userService.loginUser("Bob", "Strong_Password123")
        assertIs<Success<Cookie>>(logged2)
        val result = userService.updateUsername(newUser.value.id, "admin")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameAlreadyExists, result.value)
    }

    @Test
    fun `logoutUser should succed`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val result = userService.logoutUser(logged.value.token)
        assertIs<Success<Unit>>(result)
    }

    @Test
    fun `multiple sessions`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val logged2 = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged2)
        val result = userService.logoutUser(logged.value.token)
        assertIs<Success<Unit>>(result)
        val result2 = userService.logoutUser(logged2.value.token)
        assertIs<Success<Unit>>(result2)
    }

    @Test
    fun `create user with non existent invitation should return InvitationNotFound`() {
        val admin =
            userService.createUser(
                "admin",
                "Strong_Password123",
                "admin@mail.com",
                999999999.toString(),
            )
        assertIs<Failure<UserError>>(admin)
        assertEquals(UserError.InvitationNotFound, admin.value)
    }
/*
    @Test
    fun `tries to logout with a sessionExpired`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        // TODO val session =
    }
 */

    @Test
    fun `deleteUSer should return UserNotFound when user is not found`() {
        val result = userService.deleteUser(1)
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UserNotFound, result.value)
    }

    @Test
    fun `deleteUSer should succeeds`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val delete = userService.deleteUser(admin.value.id)
        assertIs<Success<Unit>>(delete)
    }

    @Test
    fun `getUserByToken should return null when no session was found`() {
        val result = userService.getUserByToken("invalidToken")
        assertEquals(null, result)
    }

    @Test
    fun `getUserByToken should return user when session was found`() {
        val admin =
            userService.addFirstUser(
                "admin",
                "admin@mail.com",
                "Strong_Password123",
            )
        assertIs<Success<User>>(admin)
        val logged = userService.loginUser("admin", "Strong_Password123")
        assertIs<Success<Cookie>>(logged)
        val result = userService.getUserByToken(logged.value.token)
        assertEquals(admin.value, result)
    }

    @Test
    fun `registerPDM should succeed and return user`() {
        val result = userService.registerPDM("Bob", "bob@email.com", "Strong_Password123")
        assertIs<Success<User>>(result)
        assertEquals("Bob", result.value.username)
        assertEquals("bob@email.com", result.value.email)
    }

    @Test
    fun `registerPDM should return UsernameCannotBeBlank when username is blank`() {
        val result = userService.registerPDM("", "bob@example.com", "Strong_Password123")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameCannotBeBlank, result.value)
    }

    @Test
    fun `registerPDM should return UsernameToLong when username is greater than MAX_USERNAME_LENGTH characters`() {
        val result =
            userService.registerPDM("a".repeat(User.MAX_USERNAME_LENGTH + 1), "bob@example.com", "Strong_Password123")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.UsernameToLong, result.value)
    }

    @Test
    fun `registerPDM should return InvalidEmail when email is invalid`() {
        val result = userService.registerPDM("Bob", "bob", "Strong_Password123")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.InvalidEmail, result.value)
    }

    @Test
    fun `registerPDM should return EmailCannotBeBlank when email is blank`() {
        val result = userService.registerPDM("Bob", "", "Strong_Password123")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.EmailCannotBeBlank, result.value)
    }
    @Test
    fun `registerPDM should return PasswordCannotBeBlank when password is blank`() {
        val result = userService.registerPDM("Bob", "bob@example.com", "")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.PasswordCannotBeBlank, result.value)
    }

    @Test
    fun `registerPDM should return UsernameAlreadyExists when username already exists`() {
        val result = userService.registerPDM("Bob", "bob@example.com", "Strong_Password123")
        assertIs<Success<User>>(result)
        val result2 = userService.registerPDM("Bob", "bob123@example.com", "Strong_Password123")
        assertIs<Failure<UserError>>(result2)
        assertEquals(UserError.UsernameAlreadyExists, result2.value)
    }

    @Test
    fun `registerPDM should return EmailAlreadyInUse when email already exists`() {
        val result = userService.registerPDM("Bob", "bob@example.com", "Strong_Password123")
        assertIs<Success<User>>(result)
        val result2 = userService.registerPDM("Bob2", "bob@example.com", "Strong_Password123")
        assertIs<Failure<UserError>>(result2)
        assertEquals(UserError.EmailAlreadyInUse, result2.value)
    }

    @Test
    fun `registerPDM should return WeakPassword when password is weak`() {
        val result = userService.registerPDM("Bob", "bob@example.com", "weak")
        assertIs<Failure<UserError>>(result)
        assertEquals(UserError.WeakPassword, result.value)
    }
}
