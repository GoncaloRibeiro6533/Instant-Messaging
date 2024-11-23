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
import pt.isel.controllers.MessageController
import pt.isel.controllers.UserController
import pt.isel.models.MessageHistoryOutputModel
import pt.isel.models.MessageInputModel
import pt.isel.models.MessageOutputModel
import pt.isel.models.channel.ChannelOutputModel
import pt.isel.models.channel.CreateChannelInputModel
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class MessageControllerTests {
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

        private fun createEmitters(trxManager: TransactionManager) = UpdatesEmitter(trxManager)
        private fun createMessageService(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            MessageService(trxManager, emitter)

        private fun createMessageController(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            MessageController(createMessageService(trxManager, emitter))

        private fun createUserController(trxManager: TransactionManager) =
            UserController(createUserService(trxManager, TestClock()))

        private fun createChannelService(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            ChannelService(trxManager, emitter)

        private fun createChannelController(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            ChannelController(createChannelService(trxManager, emitter))
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when sending a message to a channel, then the message is sent`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val messageController = createMessageController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val userLoggedIn =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelId =
            (
                channelController.createChannel(
                    CreateChannelInputModel("channel", Visibility.PUBLIC),
                    userLoggedIn,
                ).body as ChannelOutputModel
            ).id

        val messageInputModel = MessageInputModel(channelId, "Hello, World!")

        val result =
            messageController.sendMessage(
                messageInputModel,
                userLoggedIn,
            )

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals("Hello, World!", (result.body as MessageOutputModel).content)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when getting a message by id, then the message is returned`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val messageController = createMessageController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "email@test.com", "Admin_123dsad"),
        ).body as User

        val userLoggedIn =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelId =
            (
                channelController.createChannel(
                    CreateChannelInputModel("channel", Visibility.PUBLIC),
                    userLoggedIn,
                ).body as ChannelOutputModel
            ).id

        val messageInputModel = MessageInputModel(channelId, "Hello, World!")

        val message =
            messageController.sendMessage(
                messageInputModel,
                userLoggedIn,
            ).body as MessageOutputModel

        val result =
            messageController.getMessageById(
                message.msgId,
                userLoggedIn,
            )

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Hello, World!", (result.body as MessageOutputModel).content)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when getting a message history, then the message history is returned`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val messageController = createMessageController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "email@test.com", "Admin_123dsad"),
        ).body as User

        val userLoggedIn =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelId =
            (
                channelController.createChannel(
                    CreateChannelInputModel("channel", Visibility.PUBLIC),
                    userLoggedIn,
                ).body as ChannelOutputModel
            ).id

        val messageInputModel1 = MessageInputModel(channelId, "Msg 1")
        val messageInputModel2 = MessageInputModel(channelId, "Msg 2")
        val messageInputModel3 = MessageInputModel(channelId, "Msg 3")
        val messageInputModel4 = MessageInputModel(channelId, "Msg 4")
        val messageInputModel5 = MessageInputModel(channelId, "Msg 5")

        messageController.sendMessage(
            messageInputModel1,
            userLoggedIn,
        )

        messageController.sendMessage(
            messageInputModel2,
            userLoggedIn,
        )

        messageController.sendMessage(
            messageInputModel3,
            userLoggedIn,
        )

        messageController.sendMessage(
            messageInputModel4,
            userLoggedIn,
        )

        messageController.sendMessage(
            messageInputModel5,
            userLoggedIn,
        )

        val result =
            messageController.getMsgHistory(
                channelId,
                10,
                0,
                userLoggedIn,
            )

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(5, (result.body as MessageHistoryOutputModel).nrOfMessages)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when getting a message history with limit and skip, then the message history is returned`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val messageController = createMessageController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "email@test.com", "Admin_123dsad"),
        ).body as User

        val userLoggedIn =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelId =
            (
                channelController.createChannel(
                    CreateChannelInputModel("channel", Visibility.PUBLIC),
                    userLoggedIn,
                ).body as ChannelOutputModel
            ).id

        val messageInputModel1 = MessageInputModel(channelId, "Msg 1")
        val messageInputModel2 = MessageInputModel(channelId, "Msg 2")
        val messageInputModel3 = MessageInputModel(channelId, "Msg 3")
        val messageInputModel4 = MessageInputModel(channelId, "Msg 4")
        val messageInputModel5 = MessageInputModel(channelId, "Msg 5")

        messageController.sendMessage(
            messageInputModel1,
            userLoggedIn,
        )

        messageController.sendMessage(
            messageInputModel2,
            userLoggedIn,
        )

        messageController.sendMessage(
            messageInputModel3,
            userLoggedIn,
        )

        messageController.sendMessage(
            messageInputModel4,
            userLoggedIn,
        )

        messageController.sendMessage(
            messageInputModel5,
            userLoggedIn,
        )

        val result =
            messageController.getMsgHistory(
                channelId,
                3,
                2,
                userLoggedIn,
            )

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(3, (result.body as MessageHistoryOutputModel).nrOfMessages)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when getting a message history with invalid channel id, then a NOT_FOUND is returned`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val messageController = createMessageController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "email@test.com", "Admin_123dsad"),
        ).body as User

        val userLoggedIn =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        channelController.createChannel(
            CreateChannelInputModel("channel", Visibility.PUBLIC),
            userLoggedIn,
        )

        val result =
            messageController.getMsgHistory(
                1653,
                10,
                0,
                userLoggedIn,
            )

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when getting a message with invalid id, then a not found is returned`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val messageController = createMessageController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "email@test.com", "Admin_123dsad"),
        ).body as User

        val userLoggedIn =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        channelController.createChannel(
            CreateChannelInputModel("channel", Visibility.PUBLIC),
            userLoggedIn,
        )

        val result =
            messageController.getMessageById(
                1,
                userLoggedIn,
            )

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when sending a message with invalid channel id then a bad request is returned`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val messageController = createMessageController(trxManager, emitter)
        val userController = createUserController(trxManager)

        userController.registerFirstUser(
            UserRegisterInput("admin", "email@test.com", "Admin_123dsad"),
        ).body as User

        val userLoggedIn =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val messageInputModel = MessageInputModel(165231, "Hello, World!")

        val result =
            messageController.sendMessage(
                messageInputModel,
                userLoggedIn,
            )

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when sending a message with black content then a bad request is returned`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val messageController = createMessageController(trxManager, emitter)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "email@test.com", "Admin_123dsad"),
        ).body as User

        val userLoggedIn =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            ).body as AuthenticatedUser

        val channelId =
            (
                channelController.createChannel(
                    CreateChannelInputModel("channel", Visibility.PUBLIC),
                    userLoggedIn,
                ).body as ChannelOutputModel
            ).id

        val messageInputModel = MessageInputModel(channelId, "")

        val result =
            messageController.sendMessage(
                messageInputModel,
                userLoggedIn,
            )

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }
}
