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

        private fun createInvitationService(trxManager: TransactionManager) = InvitationService(trxManager)

        private fun createInvitationController(trxManager: TransactionManager) = InvitationController(createInvitationService(trxManager))

        private fun createChannelController(trxManager: TransactionManager) = ChannelController(createChannelService(trxManager))

        private fun createChannelService(trxManager: TransactionManager) = ChannelService(trxManager)

        private fun createUserController(trxManager: TransactionManager) = UserController(createUserService(trxManager, TestClock()))
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when creating a register invitation then it should return the invitation`(trxManager: TransactionManager) {
        val controllerInvitation = createInvitationController(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager)

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
        val invitationController = createInvitationController(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val sender =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelToRegister =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PUBLIC,
                ),
                sender,
            ).body as ChannelOutputModel

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel2",
                    Visibility.PUBLIC,
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
            ).body as RegisterInvitation

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
                    "receiver@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            )

        assertEquals(HttpStatus.CREATED, channelInvitation.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when accepting a channel invitation then it should return the invitation`(trxManager: TransactionManager) {
        val invitationController = createInvitationController(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@test.com", "Admin_123dsad"),
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
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "receiver@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as RegisterInvitation

        userController.register(
            UserRegisterInput("receiver", "receiver@test.com", "Admin_123dsad"),
            registerInvitation.id,
        )

        val receiver =
            userController.login(UserLoginCredentialsInput("receiver", "Admin_123dsad")).body as AuthenticatedUser

        val channelInvitation =
            invitationController.createChannelInvitation(
                InvitationInputModelChannel(
                    receiver.user.id,
                    "receiver@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as ChannelInvitation

        val result = invitationController.acceptChannelInvitation(channelInvitation.id)
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `decline invitation`(trxManager: TransactionManager) {
        val invitationController = createInvitationController(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@test.com", "Admin_123dsad"),
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
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "receiver@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as RegisterInvitation

        userController.register(
            UserRegisterInput("receiver", "receiver@test.com", "Admin_123dsad"),
            registerInvitation.id,
        )

        val receiver =
            userController.login(UserLoginCredentialsInput("receiver", "Admin_123dsad")).body as AuthenticatedUser

        val channelInvitation =
            invitationController.createChannelInvitation(
                InvitationInputModelChannel(
                    receiver.user.id,
                    "receiver@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as ChannelInvitation

        val result = invitationController.declineInvitation(channelInvitation.id, receiver)
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when getting a list of invitations then it should return the list`(trxManager: TransactionManager) {
        val invitationController = createInvitationController(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager)

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
                    Visibility.PUBLIC,
                ),
                sender,
            ).body as ChannelOutputModel

        val registerInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "admin@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as RegisterInvitation

        userController.register(
            UserRegisterInput("receiver", "receiver@test.com", "Admin_123dsad"),
            registerInvitation.id,
        )

        val receiver =
            userController.login(
                UserLoginCredentialsInput("receiver", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelInvitation1 =
            invitationController.createChannelInvitation(
                InvitationInputModelChannel(
                    receiver.user.id,
                    "receiver@gmail.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as ChannelInvitation

        val registerInvitation2 =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "sender2@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                sender,
            ).body as RegisterInvitation

        userController.register(
            UserRegisterInput("sender2", "sender2@test.com", "Admin_123dsad"),
            registerInvitation2.id,
        )

        val sender2 =
            userController.login(
                UserLoginCredentialsInput("sender2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel2 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel2",
                    Visibility.PUBLIC,
                ),
                sender2,
            ).body as ChannelOutputModel

        val channelInvitation2 =
            invitationController.createChannelInvitation(
                InvitationInputModelChannel(
                    receiver.user.id,
                    "receiver@test.com",
                    channel2.id,
                    Role.READ_WRITE,
                ),
                sender2,
            ).body as ChannelInvitation

        val result = invitationController.getInvitations(receiver)
        assertEquals(HttpStatus.OK, result.statusCode)
    }
}
