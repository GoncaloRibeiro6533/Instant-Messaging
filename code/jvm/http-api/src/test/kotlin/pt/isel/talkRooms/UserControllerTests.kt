@file:Suppress("ktlint")

package pt.isel.talkRooms

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.*
import pt.isel.controllers.ChannelController
import pt.isel.controllers.InvitationController
import pt.isel.controllers.UserController
import pt.isel.models.channel.ChannelOutputModel
import pt.isel.models.channel.CreateChannelInputModel
import pt.isel.models.invitation.InvitationInputModelRegister
import pt.isel.models.invitation.InvitationOutputModelRegister
import pt.isel.models.user.UserList
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class UserControllerTests {
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also { cleanup(it) },
                TransactionManagerJdbi(jdbi).also { cleanup(it) },
            )

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run {
                messageRepo.clear()
                channelRepo.clear()
                invitationRepo.clear()
                sessionRepo.clear()
                userRepo.clear()
            }
        }
    }

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
    private fun createEmitters(trxManager: TransactionManager) = UpdatesEmitter(trxManager)
    private fun createChannelService(trxManager: TransactionManager, emitter: UpdatesEmitter) =
        ChannelService(trxManager, emitter)
    private fun createChannelController(trxManager: TransactionManager, emitter: UpdatesEmitter) =
        ChannelController(createChannelService(trxManager, emitter))

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

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add first user`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        controllerUser.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).let { resp ->
            assertEquals(HttpStatus.CREATED, resp.statusCode)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add first user with invalid email`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))

        // when: creating an user
        // then: the response is a 400 with a proper Location header
        controllerUser.registerFirstUser(
            UserRegisterInput("admin", "adminmail.com", "Admin_123dsad"),
        ).let { resp ->
            assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add first user with invalid password`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))

        // when: creating an user
        // then: the response is a 400 with a proper Location header
        controllerUser.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "1234"),
        )
            .let { resp ->
                assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
            }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register first user then login`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        controllerUser.registerFirstUser(
            UserRegisterInput("admin2", "admin123@mail.com", "Admin_123dsad"),
        ).let { resp ->
            assertEquals(HttpStatus.CREATED, resp.statusCode)
        }

        // when: login with the created user
        // then: the response is a 200 with a proper Location header
        controllerUser.login(
            UserLoginCredentialsInput("admin2", "Admin_123dsad"),
        ).let { resp ->
            assertEquals(HttpStatus.OK, resp.statusCode)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register user with invitation then login`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))
        val channelController = createChannelController(trxManager, emitter = createEmitters(trxManager))
        controllerUser.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        )
            .let { resp ->
                assertEquals(HttpStatus.CREATED, resp.statusCode)
            }
        val login = controllerUser.login(
            UserLoginCredentialsInput("admin", "Admin_123dsad"),
        )
        val user = login.body as User
        val header= login.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]
        val channelToRegister =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel
        val emitter = createEmitters(trxManager)
        val invitation =
            InvitationController(createInvitationService(trxManager, emitter = emitter))
                .createRegisterInvitation(
                InvitationInputModelRegister(
                    "receiver@test.com",
                    channelToRegister.id,
                    Role.READ_ONLY,
                ),
                user = AuthenticatedUser(user, cookie),
            ).body as InvitationOutputModelRegister
        val code = trxManager.run {
            invitationRepo.findRegisterInvitationById(invitation.id)?.code
        }
        assertNotNull(code)
        val newUser =
            controllerUser.register(
                UserRegisterInput("receiver", "receiver@test.com", "Admin_123dsad"),
                code,
            )
        assertEquals(HttpStatus.CREATED, newUser.statusCode)

        val receiver =
            controllerUser.login(
                UserLoginCredentialsInput("receiver", "Admin_123dsad"),
            )
        val receiverUser = receiver.body as User
        val headerR= receiver.headers["Set-Cookie"].toString()
        assertNotNull(headerR)
        val cookieR = header.split(";")[0]
        assertEquals("receiver", receiverUser.username)
        assertEquals("receiver@test.com", receiverUser.email)
    }


    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `login should succeed`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))
        val user = controllerUser.registerFirstUser(
            UserRegisterInput("Bob", "bob@example.com", "Strong_password_1234"))
        assertEquals(HttpStatus.CREATED, user.statusCode)
        val token = controllerUser.login(
            UserLoginCredentialsInput("Bob", "Strong_password_1234")
        )
        assertEquals(HttpStatus.OK, token.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `login should fail with bad request`(trxManager: TransactionManager){
        val controller = UserController(createUserService(trxManager, TestClock()))
        val firstUser = controller.registerFirstUser(
            UserRegisterInput("Alice", "alice@example.com", "Bob_123dsad")
        )
        assertEquals(HttpStatus.CREATED, firstUser.statusCode)
        val newUser = controller.registerFirstUser(
        UserRegisterInput("Bob", "bob@example.com", "Bob_123dsad")
        )
        assertEquals(HttpStatus.CONFLICT, newUser.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register user with pdm endpoint should succeed`(trxManager: TransactionManager){
        val controllerUser = UserController(createUserService(trxManager, TestClock()))
        val newUser = controllerUser.registerPDM(
            UserRegisterInput("Bob", "bob@example.com", "Strong_password_1234")
        )
        assertEquals(HttpStatus.CREATED, newUser.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register user with pdm endpoint should fail`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))
        val newUser = controllerUser.registerPDM(
            UserRegisterInput("Bob", "bob@example.com", "1234")
        )
        assertEquals(HttpStatus.BAD_REQUEST, newUser.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `search user with username`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))
        controllerUser.registerFirstUser(
            UserRegisterInput("admin", "admin@example.com", "Admin_123dsad"))
        val token = controllerUser.login(
            UserLoginCredentialsInput("admin", "Admin_123dsad")
        )
        val user = token.body as User
        val header= token.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]
        assertIs<AuthenticatedUser>(AuthenticatedUser(user, cookie))
        val searchResult = controllerUser.searchUser("admin", 10, 0, AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.OK, searchResult.statusCode)
        assertEquals(1, (searchResult.body as UserList).users.size)
        assertEquals("admin", (searchResult.body as UserList).users[0].username)
    }

}
