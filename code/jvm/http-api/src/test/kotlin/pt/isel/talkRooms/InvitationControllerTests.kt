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
import pt.isel.models.invitation.InvitationInputModelChannel
import pt.isel.models.invitation.InvitationInputModelRegister
import pt.isel.models.invitation.InvitationOutputModelChannel
import pt.isel.models.invitation.InvitationOutputModelRegister
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class InvitationControllerTests {
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
                invitationRepo.clear()
                messageRepo.clear()
                channelRepo.clear()
                sessionRepo.clear()
                userRepo.clear()
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
            emitter
        )

        private fun createInvitationController(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            InvitationController(createInvitationService(trxManager, emitter = emitter))

        private fun createChannelController(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            ChannelController(createChannelService(trxManager, emitter))

        private fun createChannelService(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            ChannelService(trxManager, emitter)

        private fun createUserController(trxManager: TransactionManager) =
            UserController(createUserService(trxManager, TestClock()))
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when creating a register invitation then it should return the invitation`(trxManager: TransactionManager) {
        val emitter = UpdatesEmitter(trxManager)
        val controllerInvitation = createInvitationController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        // when: creating an user
        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val sender =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PUBLIC,
                ),
                sender,
            ).body as ChannelOutputModel

        val registerInvitation =
            controllerInvitation.createRegisterInvitation(
                InvitationInputModelRegister(
                    "receiver@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            )
        assertEquals(HttpStatus.CREATED, registerInvitation.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when creating a channel invitation then it should return the invitation`(trxManager: TransactionManager) {
        val emitter = UpdatesEmitter(trxManager)
        val invitationController = createInvitationController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val sender =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelToRegister =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                sender,
            ).body as ChannelOutputModel

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel2",
                    Visibility.PRIVATE,
                ),
                sender,
            ).body as ChannelOutputModel

        val registerInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "receiver@test.com",
                    channelToRegister.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput("receiver", "receiver@test.com", "Admin_123dsad"),
            registerInvitation.id,
        )

        val receiver =
            userController.login(
                UserLoginCredentialsInput("receiver", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelInvitation =
            invitationController.createChannelInvitation(
                InvitationInputModelChannel(
                    receiver.user.id,
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            )

        assertEquals(HttpStatus.CREATED, channelInvitation.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when creating an invitation with an invalid email then it should return an error`(trxManager: TransactionManager) {
        val emitter = UpdatesEmitter(trxManager)
        val invitationController = createInvitationController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@test.com", "Admin_123dsad"),
        ).body as User

        val sender =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel = channelController.createChannel(
            CreateChannelInputModel(
                "channel1",
                Visibility.PUBLIC,
            ),
            sender,
        ).body as ChannelOutputModel

        val result = invitationController.createRegisterInvitation(
            InvitationInputModelRegister(
                "invalidEmail",
                channel.id,
                Role.READ_WRITE,
            ),
            sender,
        )

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)

    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when creating an invitation with an email that already exists then it should return an error`(trxManager: TransactionManager) {
        val emitter = UpdatesEmitter(trxManager)
        val invitationController = createInvitationController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@test.com", "Admin_123dsad"),
        ).body as User

        val sender =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel = channelController.createChannel(
            CreateChannelInputModel(
                "channel1",
                Visibility.PUBLIC,
            ),
            sender,
        ).body as ChannelOutputModel

        val regInvitation = invitationController.createRegisterInvitation(
            InvitationInputModelRegister(
                "receiver@test.com",
                channel.id,
                Role.READ_WRITE,
            ),
            sender,
        ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput("receiver", "receiver@test.com", "Admin_123dsad"),
            regInvitation.id,
        ).body as User

        val receiver = userController.login(
            UserLoginCredentialsInput("receiver", "Admin_123dsad"),
        ).body as AuthenticatedUser

        val result = invitationController.createChannelInvitation(
            InvitationInputModelChannel(
                receiver.user.id,
                channel.id,
                Role.READ_WRITE,
            ),
            sender,
        )

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)

    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when accepting a channel invitation then it should return the invitation`(trxManager: TransactionManager) {
        val emitter = UpdatesEmitter(trxManager)
        val invitationController = createInvitationController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@test.com", "Admin_123dsad"),
        ).body as? User ?: throw AssertionError("User registration failed")

        val sender = userController.login(
            UserLoginCredentialsInput("admin", "Admin_123dsad"),
        ).body as? AuthenticatedUser ?: throw AssertionError("User login failed")

        val channelForRegister = channelController.createChannel(
            CreateChannelInputModel(
                "channel1",
                Visibility.PRIVATE,
            ),
            sender,
        ).body as? ChannelOutputModel ?: throw AssertionError("Channel creation failed")

        val registerInvitation = invitationController.createRegisterInvitation(
            InvitationInputModelRegister(
                "receiver@test.com",
                channelForRegister.id,
                Role.READ_WRITE,
            ),
            sender,
        ).body as? InvitationOutputModelRegister ?: throw AssertionError("Register invitation creation failed")

        userController.register(
            UserRegisterInput("receiver", "receiver@test.com", "Admin_123dsad"),
            registerInvitation.id,
        )

        val channelForInviting = channelController.createChannel(
            CreateChannelInputModel(
                "channel2",
                Visibility.PRIVATE,
            ),
            sender,
        ).body as? ChannelOutputModel ?: throw AssertionError("Channel creation failed")

        val receiver = userController.login(
            UserLoginCredentialsInput("receiver", "Admin_123dsad"),
        ).body as? AuthenticatedUser ?: throw AssertionError("Receiver login failed")

        val channelInvitation = invitationController.createChannelInvitation(
            InvitationInputModelChannel(
                receiver.user.id,
                channelForInviting.id,
                Role.READ_WRITE,
            ),
            sender,
        ).body as? InvitationOutputModelChannel ?: throw AssertionError("Channel invitation creation failed")

        val result = invitationController.acceptChannelInvitation(channelInvitation.id, receiver)
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when accepting another person's channel invitation it should return an error`(trxManager: TransactionManager) {
        val emitter = UpdatesEmitter(trxManager)
        val invitationController = createInvitationController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@test.com", "Admin_123dsad"),
        ).body as User

        val sender =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelForRegister = channelController.createChannel(
            CreateChannelInputModel(
                "channel1",
                Visibility.PRIVATE,
            ),
            sender,
        ).body as ChannelOutputModel

        val registerInvitation1 = invitationController.createRegisterInvitation(
            InvitationInputModelRegister(
                "receiver1@test.com",
                channelForRegister.id,
                Role.READ_WRITE,
            ),
            sender,
        ).body as InvitationOutputModelRegister

        val registerInvitation2 = invitationController.createRegisterInvitation(
            InvitationInputModelRegister(
                "wr@test.com",
                channelForRegister.id,
                Role.READ_WRITE,
            ),
            sender,
        ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput("receiver1", "receiver1@test.com", "Admin_123dsad"),
            registerInvitation1.id,
        ).body as User

        val receiver1 = userController.login(
            UserLoginCredentialsInput("receiver1", "Admin_123dsad"),
        ).body as AuthenticatedUser

        userController.register(
            UserRegisterInput("wrongReceiver", "wr@test.com", "Admin_123dsad"),
            registerInvitation2.id,
        ).body as User

        val wrongReceiver = userController.login(
            UserLoginCredentialsInput("wrongReceiver", "Admin_123dsad"),
        ).body as AuthenticatedUser

        val channelForInviting = channelController.createChannel(
            CreateChannelInputModel(
                "channel2",
                Visibility.PRIVATE,
            ),
            sender,
        ).body as ChannelOutputModel

        val channelInvitation = invitationController.createChannelInvitation(
            InvitationInputModelChannel(
                receiver1.user.id,
                channelForInviting.id,
                Role.READ_WRITE,
            ),
            sender,
        ).body as InvitationOutputModelChannel

        val result = invitationController.acceptChannelInvitation(channelInvitation.id, wrongReceiver)
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)

    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `decline invitation`(trxManager: TransactionManager) {
        val emitter = UpdatesEmitter(trxManager)
        val invitationController = createInvitationController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@test.com", "Admin_123dsad"),
        ).body as User

        val sender =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelForRegister =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                sender,
            ).body as ChannelOutputModel

        val registerInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "receiver@test.com",
                    channelForRegister.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput("receiver", "receiver@test.com", "Admin_123dsad"),
            registerInvitation.id,
        )

        val receiver =
            userController.login(UserLoginCredentialsInput("receiver", "Admin_123dsad")).body as AuthenticatedUser

        val channelForInviting =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel2",
                    Visibility.PRIVATE,
                ),
                sender,
            ).body as ChannelOutputModel

        val channelInvitation =
            invitationController.createChannelInvitation(
                InvitationInputModelChannel(
                    receiver.user.id,
                    channelForInviting.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as InvitationOutputModelChannel


        val result = invitationController.declineInvitation(channelInvitation.id, receiver)
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when declining someone else's invitation, it should return unauthorized`(trxManager: TransactionManager) {
        val emitter = UpdatesEmitter(trxManager)
        val invitationController = createInvitationController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@tes.com", "Admin_123dsad"),
        ).body as User

        val sender =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser


        val channelForRegister = channelController.createChannel(
            CreateChannelInputModel(
                "channel1",
                Visibility.PRIVATE,
            ),
            sender,
        ).body as ChannelOutputModel

        val registerInvitation = invitationController.createRegisterInvitation(
            InvitationInputModelRegister(
                "receiver1@test.com",
                channelForRegister.id,
                Role.READ_WRITE,
            ),
            sender,
        ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput("receiver1", "receiver1@test.com", "Admin_123dsad"),
            registerInvitation.id,
        ).body as User

        val receiver1 = userController.login(
            UserLoginCredentialsInput("receiver1", "Admin_123dsad"),
        ).body as AuthenticatedUser

        val channelForInviting = channelController.createChannel(
            CreateChannelInputModel(
                "channel2",
                Visibility.PRIVATE,
            ),
            sender,
        ).body as ChannelOutputModel

        val channelInvitation = invitationController.createChannelInvitation(
            InvitationInputModelChannel(
                receiver1.user.id,
                channelForInviting.id,
                Role.READ_WRITE,
            ),
            sender,
        ).body as InvitationOutputModelChannel

        val result = invitationController.declineInvitation(channelInvitation.id, sender)
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)

    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when getting a list of invitations then it should return the list`(trxManager: TransactionManager) {
        val emitter = UpdatesEmitter(trxManager)
        val invitationController = createInvitationController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@tes.com", "Admin_123dsad"),
        ).body as User

        val sender =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                sender,
            ).body as ChannelOutputModel

        val registerInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "receiver@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput("receiver", "receiver@test.com", "Admin_123dsad"),
            registerInvitation.id,
        )

        val receiver =
            userController.login(
                UserLoginCredentialsInput("receiver", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val registerInvitation2 =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "sender@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput("sender", "sender@test.com", "Admin_123dsad"),
            registerInvitation2.id,
        )

        val sender2 =
            userController.login(
                UserLoginCredentialsInput("sender", "Admin_123dsad"),
            ).body as AuthenticatedUser

        channelController.createChannel(
            CreateChannelInputModel(
                "channel2",
                Visibility.PUBLIC,
            ),
            sender2,
        ).body as ChannelOutputModel

        val result = invitationController.getInvitations(receiver)
        assertEquals(HttpStatus.OK, result.statusCode)
    }
}
