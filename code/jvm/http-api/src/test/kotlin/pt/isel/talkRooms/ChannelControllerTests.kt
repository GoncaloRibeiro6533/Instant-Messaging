@file:Suppress("ktlint")

package pt.isel.talkRooms

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.*
import pt.isel.controllers.ChannelController
import pt.isel.controllers.InvitationController
import pt.isel.controllers.UserController
import pt.isel.models.channel.*
import pt.isel.models.invitation.InvitationInputModelChannel
import pt.isel.models.invitation.InvitationInputModelRegister
import pt.isel.models.invitation.InvitationOutputModelChannel
import pt.isel.models.invitation.InvitationOutputModelRegister
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput
import java.util.stream.Stream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class ChannelControllerTests {
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        private val logger = LoggerFactory.getLogger(ChannelControllerTests::class.java)

        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also {
                    cleanup(it)
                    logger.info("## Cleaned up InMem")
                                               },
                TransactionManagerJdbi(jdbi).also { cleanup(it)
                    logger.info("## Cleaned up JDBI")
                                                  },
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
            emitter = emitter
        )

        private fun  createEmitters(trxManager: TransactionManager) =
            UpdatesEmitter(trxManager)

        private fun createInvitationController(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            InvitationController(createInvitationService(trxManager, emitter = emitter))

        private fun createChannelController(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            ChannelController(createChannelService(trxManager, emitter))

        private fun createChannelService(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            ChannelService(trxManager,emitter)

        private fun createUserController(trxManager: TransactionManager) =
            UserController(createUserService(trxManager, TestClock()))
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create channel should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val result =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                channelCreator,
            )

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assert(result.body is ChannelOutputModel)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel by id should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                channelCreator,
            ).body as ChannelOutputModel

        val result = channelController.getChannelById(channel.id, channelCreator)
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel by name should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        channelController.createChannel(
            CreateChannelInputModel(
                "channel1",
                Visibility.PRIVATE,
            ),
            channelCreator,
        ).body as ChannelOutputModel

        val result = channelController.getChannelByName("channel1", channelCreator, 10, 0)
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel members should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                channelCreator,
            ).body as ChannelOutputModel

        val member1RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "member1@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                channelCreator,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput(
                "member1",
                "member1@test.com",
                "Member_123dsad",
            ),
            member1RegisterInvitation.id,
        ).body as User

        val result = channelController.getChannelMembers(channel.id, channelCreator)
        assertEquals(HttpStatus.OK, result.statusCode)

        val members = result.body as ChannelMembersList
        assertEquals(2, members.nMembers)
        assertEquals(Role.READ_WRITE, members.members.first { it.user.username == "member1" }.role)
        assertEquals(Role.READ_WRITE, members.members.first { it.user.username == "admin2" }.role)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel members of non existing channel should return error`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val result = channelController.getChannelMembers(0, channelCreator)
        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channels of user should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val user1 =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel1 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                user1,
            ).body as ChannelOutputModel

        channelController.createChannel(
            CreateChannelInputModel(
                "channel2",
                Visibility.PRIVATE,
            ),
            user1,
        ).body as ChannelOutputModel

        val member1RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "user2@test.com",
                    channel1.id,
                    Role.READ_WRITE,
                ),
                user1,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput(
                "user2",
                "user2@test.com",
                "User_123dsad",
            ),
            member1RegisterInvitation.id,
        ).body as User

        val user2 =
            userController.login(
                UserLoginCredentialsInput("user2", "User_123dsad"),
            ).body as AuthenticatedUser

        val channel3 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel3",
                    Visibility.PRIVATE,
                ),
                user2,
            ).body as ChannelOutputModel

        val channelInvitation =
            invitationController.createChannelInvitation(
                InvitationInputModelChannel(
                    user1.user.id,
                    channel3.id,
                    Role.READ_WRITE,
                ),
                user2,
            ).body as InvitationOutputModelChannel

        invitationController.acceptChannelInvitation(
            channelInvitation.id,
            user1,
        )

        val result = channelController.getChannelsOfUser(user1.user.id, user1)
        assertEquals(HttpStatus.OK, result.statusCode)

        val channels = result.body as ChannelOfUserList
        assertEquals(3, channels.nChannels)
        assertEquals(Role.READ_WRITE, channels.channels.first { it.channel.name == "channel1" }.role)
        assertEquals(Role.READ_WRITE, channels.channels.first { it.channel.name == "channel2" }.role)
        assertEquals(Role.READ_WRITE, channels.channels.first { it.channel.name == "channel3" }.role)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update channel's name should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                admin,
            ).body as ChannelOutputModel

        val result = channelController.updateChannelName(channel.id, "channel123", admin)
        assertEquals(HttpStatus.OK, result.statusCode)

        val updatedChannel = channelController.getChannelById(channel.id, admin).body as ChannelOutputModel
        assertEquals("channel123", updatedChannel.name)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update name of non existing channel should return an error`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val result = channelController.updateChannelName(0, "channel123", admin)
        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update name of a channel where the user is not a member should return an error`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                admin,
            ).body as ChannelOutputModel

        val nonMemberRegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "nonMember@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                admin,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput(
                "nonMember",
                "nonMember@test.com",
                "NonMember_123dsad",
            ),
            nonMemberRegisterInvitation.id,
        ).body as User

        val nonMember =
            userController.login(
                UserLoginCredentialsInput("nonMember", "NonMember_123dsad"),
            ).body as AuthenticatedUser

        channelController.leaveChannel(channel.id, nonMember)

        val result = channelController.updateChannelName(channel.id, "channel123", nonMember)
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `leave channel should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                admin,
            ).body as ChannelOutputModel

        val result = channelController.leaveChannel(channel.id, admin)
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    //TODO cant add another member to channel, not implemented
    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add member to channel should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                admin,
            ).body as ChannelOutputModel

        val member1RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "member1@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                admin,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput(
                "member1",
                "member1@test.com",
                "Member_123dsad",
            ),
            member1RegisterInvitation.id,
        ).body as User

        val member1 =
            userController.login(
                UserLoginCredentialsInput("member1", "Member_123dsad"),
            ).body as AuthenticatedUser

        invitationController.createRegisterInvitation(
            InvitationInputModelRegister(
                "member2@test.com",
                channel.id,
                Role.READ_WRITE,
            ),
            admin,
        )

        val channel2 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel2",
                    Visibility.PRIVATE,
                ),
                admin,
            ).body as ChannelOutputModel

        val result =
            channelController.joinChannel(
                channel2.id,
                Role.READ_WRITE,
                member1,
            )
        assertEquals(HttpStatus.OK, result.statusCode)

        val members: ChannelMembersList = channelController.getChannelMembers(channel2.id, admin).body as ChannelMembersList
        assertEquals(2, members.nMembers)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add member to channel where the user is not in`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                admin,
            ).body as ChannelOutputModel

        val user2RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "user2@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                admin,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput(
                "user2",
                "user2@test.com",
                "User2_123dsad",
            ),
            user2RegisterInvitation.id,
        ).body as User

        val user2 =
            userController.login(
                UserLoginCredentialsInput("user2", "User2_123dsad"),
            ).body as AuthenticatedUser

        val user3RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "user3@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                admin,
            ).body as InvitationOutputModelRegister

        userController.register(
            UserRegisterInput(
                "user3",
                "user3@test.com",
                "User3_123dsad",
            ),
            user3RegisterInvitation.id,
        ).body as User

        val user3 =
            userController.login(
                UserLoginCredentialsInput("user3", "User3_123dsad"),
            ).body as AuthenticatedUser

        val channel2 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel2",
                    Visibility.PRIVATE,
                ),
                user2,
            ).body as ChannelOutputModel

        val result =
            channelController.joinChannel(
                channel2.id,
                Role.READ_WRITE,
                user3,
            )

        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createChannel should fails`(trxManager: TransactionManager){
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val result =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                channelCreator,
            )

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assert(result.body is ChannelOutputModel)
        val result2 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                channelCreator,
            )
        assertEquals(HttpStatus.CONFLICT, result2.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `addUserToChannel should fail`(trxManager: TransactionManager){
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val result = channelController.getChannelById(-1, channelCreator)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getChannelById should fail`(trxManager: TransactionManager){
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser
        val result = channelController.getChannelById(-1, channelCreator)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel by name should fail`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            ).body as AuthenticatedUser

        channelController.createChannel(
            CreateChannelInputModel(
                "channel1",
                Visibility.PRIVATE,
            ),
            channelCreator,
        ).body as ChannelOutputModel

        val result = channelController.getChannelByName("channel2", channelCreator, -1, 0)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }


}
