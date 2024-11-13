import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.Channel
import pt.isel.JdbiChannelRepository
import pt.isel.JdbiInvitationRepository
import pt.isel.JdbiMessageRepository
import pt.isel.JdbiUserRepository
import pt.isel.User
import pt.isel.Visibility
import pt.isel.configureWithAppRequirements
import java.time.LocalDateTime
import kotlin.test.assertEquals

class JbdiMessageRepositoryTest {
    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()
    }

    @BeforeEach
    fun clearDatabase() {
        runWithHandle { handle ->
            JdbiMessageRepository(handle).clear()
            JdbiInvitationRepository(handle).clear()
            JdbiChannelRepository(handle).clear()
            JdbiUserRepository(handle).clear()
        }
    }

    @Test
    fun `should create a message`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "email@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)
            val message = JdbiMessageRepository(handle).createMessage(user, channel, "text", LocalDateTime.now())

            assertEquals(message.sender.id, user.id)
            assertEquals(message.channel.id, channel.id)
            assertEquals(message.content, "text")
        }
    }

    @Test
    fun `when trying to create a message with invalid user, it should not let the action end successfully`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            assertThrows<Exception> {
                JdbiMessageRepository(handle).createMessage(
                    User(-1, "username", "invalid@invalid.com"),
                    channel,
                    "text",
                    LocalDateTime.now(),
                )
            }

            val messages = JdbiMessageRepository(handle).findByChannel(channel, 5, 0)
            assertEquals(0, messages.size)
        }
    }

    @Test
    fun `when trying to create a message with invalid channel, it should not let the action end successfully`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")

            assertThrows<Exception> {
                JdbiMessageRepository(handle).createMessage(
                    user,
                    Channel(-1, "channel1", user, Visibility.PUBLIC),
                    "text",
                    LocalDateTime.now(),
                )
            }

            val messages =
                JdbiMessageRepository(handle).findByChannel(Channel(99, "channel99", user, Visibility.PUBLIC), 5, 0)
            assertEquals(0, messages.size)
        }
    }

    @Test
    fun `should find a message by its id`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            val message = JdbiMessageRepository(handle).createMessage(user, channel, "text", LocalDateTime.now())
            val foundMessage = JdbiMessageRepository(handle).findById(message.id)

            assertEquals(message.id, foundMessage!!.id)
            assertEquals(message.sender, foundMessage.sender)
            assertEquals(message.channel, foundMessage.channel)
            assertEquals(message.content, foundMessage.content)
            assertEquals(message.timestamp.withNano(0), foundMessage.timestamp.withNano(0))
        }
    }

    @Test
    fun `when getting a message history, then the message history is returned`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            (1..10).map {
                JdbiMessageRepository(handle).createMessage(user, channel, "text$it", LocalDateTime.now())
            }

            val messageHistory = JdbiMessageRepository(handle).findByChannel(channel, 5, 0)

            assertEquals(5, messageHistory.size)
        }
    }

    @Test
    fun `when getting a message with invalid id, the database should return null`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            val message = JdbiMessageRepository(handle).createMessage(user, channel, "text", LocalDateTime.now())
            val foundMessage = JdbiMessageRepository(handle).findById(message.id + 1)

            assertEquals(null, foundMessage)
        }
    }

    @Test
    fun `getting message by channel should return the correct messages`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            (1..10).map {
                JdbiMessageRepository(handle).createMessage(user, channel, "text$it", LocalDateTime.now())
            }

            val messageHistory = JdbiMessageRepository(handle).findByChannel(channel, 5, 0)

            assertEquals(5, messageHistory.size)
        }
    }

    @Test
    fun `when getting a message with invalid channel, the database should return an empty list`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            (1..10).map {
                JdbiMessageRepository(handle).createMessage(user, channel, "text$it", LocalDateTime.now())
            }

            val messageHistory =
                JdbiMessageRepository(handle).findByChannel(Channel(99, "channel99", user, Visibility.PUBLIC), 5, 0)

            assertEquals(0, messageHistory.size)
        }
    }

    @Test
    fun `deleting a message by id successfully`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            val message = JdbiMessageRepository(handle).createMessage(user, channel, "text", LocalDateTime.now())
            val foundMessage = JdbiMessageRepository(handle).findById(message.id)

            assertEquals(message.id, foundMessage!!.id)
        }
    }

    @Test
    fun `when deleting a message with invalid id, the database should return null`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            val message = JdbiMessageRepository(handle).createMessage(user, channel, "text", LocalDateTime.now())
            val foundMessage = JdbiMessageRepository(handle).findById(message.id + 1)

            assertEquals(null, foundMessage)
        }
    }

    @Test
    fun `delete a message by channel should work correctly`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            (1..10).map {
                JdbiMessageRepository(handle).createMessage(user, channel, "text$it", LocalDateTime.now())
            }

            val messageHistory = JdbiMessageRepository(handle).findByChannel(channel, 5, 0)
            assertEquals(5, messageHistory.size)
        }
    }

    @Test
    fun `when deleting a message with invalid channel, the database should return an empty list`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            (1..10).map {
                JdbiMessageRepository(handle).createMessage(user, channel, "text$it", LocalDateTime.now())
            }

            val messageHistory =
                JdbiMessageRepository(handle).findByChannel(Channel(99, "channel99", user, Visibility.PUBLIC), 5, 0)

            assertEquals(0, messageHistory.size)
        }
    }

    @Test
    fun `find all should succeed`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            (1..10).map {
                JdbiMessageRepository(handle).createMessage(user, channel, "text$it", LocalDateTime.now())
            }

            val messageHistory = JdbiMessageRepository(handle).findAll()
            assertEquals(10, messageHistory.size)
        }
    }
}
