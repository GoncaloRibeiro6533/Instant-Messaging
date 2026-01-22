import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.Channel
import pt.isel.JdbiChannelRepository
import pt.isel.JdbiInvitationRepository
import pt.isel.JdbiMessageRepository
import pt.isel.JdbiUserRepository
import pt.isel.Role
import pt.isel.User
import pt.isel.Visibility
import pt.isel.configureWithAppRequirements
import kotlin.test.assertContains

class JdbiChannelRepositoryTests {
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
    fun `should create a channel`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            assertEquals(channel.name, "channel1")
            assertEquals(channel.creator.id, user.id)
            assertEquals(channel.visibility, Visibility.PUBLIC)
        }
    }

    @Test
    fun `when trying to create a channel with invalid user, it should not let the action end successfully`() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            assertThrows<Exception> {
                JdbiChannelRepository(handle).createChannel(
                    "channel1",
                    User(-1, "username", "user@test.com"),
                    Visibility.PUBLIC,
                )
            }
        }
    }

    @Test
    fun `should add a user to a channel`() {
        runWithHandle { handle ->
            val admin = JdbiUserRepository(handle).createUser("admin", "admin@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", admin, Visibility.PUBLIC)
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")

            JdbiChannelRepository(handle).joinChannel(user, channel, Role.READ_WRITE)

            val channelMembers = JdbiChannelRepository(handle).getChannelMembers(channel)
            assertTrue(channelMembers.containsKey(user))
        }
    }

    @Test
    fun `should not add user to non existing channel`() {
        runWithHandle { handle ->
            val admin = JdbiUserRepository(handle).createUser("admin", "admin@test.com", "password")

            assertThrows<Exception> {
                JdbiChannelRepository(handle).joinChannel(
                    User(-1, "username", "user@test.com"),
                    Channel(-1, "channel1", admin, Visibility.PUBLIC),
                    Role.READ_WRITE,
                )
            }
        }
    }

    @Test
    fun `find channel by id should succeed`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            val foundChannel = JdbiChannelRepository(handle).findById(channel.id)
            assertEquals(channel, foundChannel)
        }
    }

    @Test
    fun `find channel by id should return null if channel does not exist`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            val foundChannel = JdbiChannelRepository(handle).findById(channel.id + 1)
            assertEquals(null, foundChannel)
        }
    }

    @Test
    fun `get channel by name should succeed`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            val foundChannel = JdbiChannelRepository(handle).getChannelByName("channel1", 10, 0)
            assertEquals(listOf(channel), foundChannel)
        }
    }

    @Test
    fun `get channel by name should return empty list if channel does not exist`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            val foundChannel = JdbiChannelRepository(handle).getChannelByName("channel2", 10, 0)
            assertEquals(0, foundChannel.size)
        }
    }

    @Test
    fun `get channel by name should return empty list if channel does not exist with the given limit and skip`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)

            val foundChannel = JdbiChannelRepository(handle).getChannelByName("channel2", 10, 1)
            assertEquals(0, foundChannel.size)
        }
    }

    @Test
    fun `get channel by name should return the correct number of channels with the given limit and skip`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)
            JdbiChannelRepository(handle).createChannel("channel2", user, Visibility.PUBLIC)
            JdbiChannelRepository(handle).createChannel("channel3", user, Visibility.PUBLIC)

            val foundChannel = JdbiChannelRepository(handle).getChannelByName("channel", 2, 1)
            assertEquals(2, foundChannel.size)

            val foundChannel2 = JdbiChannelRepository(handle).getChannelByName("channel", 2, 0)
            assertEquals(2, foundChannel2.size)

            val foundChannel3 = JdbiChannelRepository(handle).getChannelByName("channel", 2, 2)
            assertEquals(1, foundChannel3.size)
        }
    }

   /* @Test
    fun `get channel by name should return the correct number of channels with the given limit and skip and the correct order`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            JdbiChannelRepository(handle).createChannel("channel1", user, Visibility.PUBLIC)
            assertEquals(
                "channel1",
                JdbiChannelRepository(handle).getChannelByName("channel1", 1, 0).first().name,
            )
            JdbiChannelRepository(handle).createChannel("channel2", user, Visibility.PUBLIC)
            assertEquals(
                "channel2",
                JdbiChannelRepository(handle).getChannelByName("channel2", 1, 0).first().name,
            )
            JdbiChannelRepository(handle).createChannel("channel3", user, Visibility.PUBLIC)
            assertEquals(
                "channel3",
                JdbiChannelRepository(handle).getChannelByName("channel3", 1, 0).first().name,
            )

            val foundChannel = JdbiChannelRepository(handle).getChannelByName("channel", 2, 1)
            assertEquals(2, foundChannel.size)
            assertEquals("channel2", foundChannel[0].name)
            assertEquals("channel3", foundChannel[1].name)
            val foundChannel2 = JdbiChannelRepository(handle).getChannelByName("channel", 2, 0)
            assertEquals(2, foundChannel2.size)
            assertEquals("channel1", foundChannel2[0].name)

            val foundChannel3 = JdbiChannelRepository(handle).getChannelByName("channel", 2, 2)
            assertEquals("channel3", foundChannel3[0].name)
        }
    }*/

    @Test
    fun `get channel members should return the correct number of members`() {
        runWithHandle { handle ->
            val admin = JdbiUserRepository(handle).createUser("admin", "admin@test.com", "password")
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            val user2 = JdbiUserRepository(handle).createUser("user2", "user2@test.com", "password")

            val channel = JdbiChannelRepository(handle).createChannel("channel1", admin, Visibility.PUBLIC)
            JdbiChannelRepository(handle).joinChannel(admin, channel, Role.READ_WRITE)
            JdbiChannelRepository(handle).joinChannel(user, channel, Role.READ_WRITE)
            JdbiChannelRepository(handle).joinChannel(user2, channel, Role.READ_WRITE)

            val channelMembers = JdbiChannelRepository(handle).getChannelMembers(channel)
            assertEquals(3, channelMembers.size)
            assertContains(channelMembers, admin)
            assertContains(channelMembers, user)
            assertContains(channelMembers, user2)
        }
    }

    @Test
    fun `update channel name should succeed`() {
        runWithHandle { handle ->
            val admin = JdbiUserRepository(handle).createUser("admin", "admin@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", admin, Visibility.PUBLIC)

            val updatedChannel = JdbiChannelRepository(handle).updateChannelName(channel, "channel2")
            assertEquals("channel2", updatedChannel.name)
        }
    }

    @Test
    fun `update channel name should fail if channel does not exist`() {
        runWithHandle { handle ->
            val admin = JdbiUserRepository(handle).createUser("admin", "admin@test.com", "password")

            assertThrows<Exception> {
                JdbiChannelRepository(handle).updateChannelName(
                    Channel(-1, "channel1", admin, Visibility.PUBLIC),
                    "channel2",
                )
            }
        }
    }

    @Test
    fun `leave channel should succeed`() {
        runWithHandle { handle ->
            val admin = JdbiUserRepository(handle).createUser("admin", "admin@test.com", "password")
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", admin, Visibility.PUBLIC)
            JdbiChannelRepository(handle).joinChannel(admin, channel, Role.READ_WRITE)
            JdbiChannelRepository(handle).joinChannel(user, channel, Role.READ_WRITE)
            JdbiChannelRepository(handle).leaveChannel(user, channel)

            val channelMembers = JdbiChannelRepository(handle).getChannelMembers(channel)
            assertEquals(1, channelMembers.size)
            assertContains(channelMembers.keys, admin)
            assertTrue(!channelMembers.containsKey(user))
        }
    }

    @Test
    fun `get channels of user should return the correct number of channels`() {
        runWithHandle { handle ->
            val admin = JdbiUserRepository(handle).createUser("admin", "admin@example.com", "password")
            val user = JdbiUserRepository(handle).createUser("user", "user@example.com", "password")
            val channel1 = JdbiChannelRepository(handle).createChannel("channel1", admin, Visibility.PUBLIC)
            val channel2 = JdbiChannelRepository(handle).createChannel("channel2", user, Visibility.PUBLIC)
            JdbiChannelRepository(handle).joinChannel(user, channel1, Role.READ_WRITE)
            JdbiChannelRepository(handle).joinChannel(admin, channel1, Role.READ_WRITE)
            JdbiChannelRepository(handle).joinChannel(admin, channel2, Role.READ_ONLY)
            val channelsOfAdmin = JdbiChannelRepository(handle).getChannelsOfUser(admin)
            assertEquals(2, channelsOfAdmin.size)
            assertContains(channelsOfAdmin.keys, channel1)
            assertContains(channelsOfAdmin.keys, channel2)
            assertEquals(Role.READ_WRITE, channelsOfAdmin[channel1])
            assertEquals(Role.READ_ONLY, channelsOfAdmin[channel2])
        }
    }
    // TODO

  /*  @Test
    fun `leave channel should fail if user is not in the channel`() {
        runWithHandle { handle ->
            val admin = JdbiUserRepository(handle).createUser("admin", "admin@test.com", "password")
            val user = JdbiUserRepository(handle).createUser("user", "user@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel1", admin, Visibility.PUBLIC)

            assertThrows<Exception> {
                JdbiChannelRepository(handle).leaveChannel(user, channel)
            }
        }
    }*/
}
