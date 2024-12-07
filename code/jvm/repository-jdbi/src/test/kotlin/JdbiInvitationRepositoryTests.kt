import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
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
import java.time.LocalDateTime
import kotlin.test.Test

class JdbiInvitationRepositoryTests {
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
    fun `should create a channel invitation`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")
            val receiver = JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel", sender, Visibility.PUBLIC)

            val invitation =
                JdbiInvitationRepository(handle).createChannelInvitation(
                    sender,
                    receiver,
                    channel,
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                )
            assertEquals(invitation.sender, sender)
            assertEquals(invitation.receiver, receiver)
            assertEquals(invitation.channel, channel)
            assertEquals(invitation.role, Role.READ_WRITE)
        }
    }

    @Test
    fun `create channel invitation should fail when sender is not a member of the channel`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")
            val receiver = JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel", sender, Visibility.PUBLIC)

            assertThrows<Exception> {
                JdbiInvitationRepository(handle).createChannelInvitation(
                    receiver,
                    receiver,
                    channel,
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                )
            }
        }
    }

    @Test
    fun `create channel invitation should fail for non existing channel`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")
            val receiver = JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")

            assertThrows<Exception> {
                JdbiInvitationRepository(handle).createChannelInvitation(
                    sender,
                    receiver,
                    Channel(99, "channel", sender, Visibility.PUBLIC),
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                )
            }
        }
    }

    @Test
    fun `create channel invitation should fail for non existing sender`() {
        runWithHandle { handle ->
            val receiver = JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel", receiver, Visibility.PUBLIC)

            assertThrows<Exception> {
                JdbiInvitationRepository(handle).createChannelInvitation(
                    User(99, "sender", "sender@test.com"),
                    receiver,
                    channel,
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                )
            }
        }
    }

    @Test
    fun `create channel invitation should fail for non existing receiver`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel", sender, Visibility.PUBLIC)

            assertThrows<Exception> {
                JdbiInvitationRepository(handle).createChannelInvitation(
                    sender,
                    User(99, "receiver", "receiver@test.com"),
                    channel,
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                )
            }
        }
    }

    @Test
    fun `find register invitation should succeed`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")
            val receiver = JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel", sender, Visibility.PUBLIC)

            val invitation =
                JdbiInvitationRepository(handle).createChannelInvitation(
                    sender,
                    receiver,
                    channel,
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                )

            val foundChannelInvitation = JdbiInvitationRepository(handle).findChannelInvitationById(invitation.id)
            assertEquals(foundChannelInvitation!!.sender, sender)
            assertEquals(foundChannelInvitation.receiver, receiver)
            assertEquals(foundChannelInvitation.channel, channel)
            assertEquals(foundChannelInvitation.role, Role.READ_WRITE)
        }
    }

    @Test
    fun `find register invitation should return null for non existing invitation`() {
        runWithHandle { handle ->
            val invitation = JdbiInvitationRepository(handle).findChannelInvitationById(99)
            assertNull(invitation)
        }
    }

    @Test
    fun `find channel invitation by id should succeed`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")
            val receiver = JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel", sender, Visibility.PUBLIC)

            val invitation =
                JdbiInvitationRepository(handle).createChannelInvitation(
                    sender,
                    receiver,
                    channel,
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                )

            val foundInvitation = JdbiInvitationRepository(handle).findChannelInvitationById(invitation.id)
            assertEquals(foundInvitation!!.sender, sender)
            assertEquals(foundInvitation.receiver, receiver)
            assertEquals(foundInvitation.channel, channel)
            assertEquals(foundInvitation.role, Role.READ_WRITE)
        }
    }

    @Test
    fun `find channel invitation by id should return null for non existing invitation`() {
        runWithHandle { handle ->
            val invitation = JdbiInvitationRepository(handle).findChannelInvitationById(99)
            assertNull(invitation)
        }
    }

    @Test
    fun `update channel invitation - used - should succeed`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")
            val receiver = JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")

            val channel = JdbiChannelRepository(handle).createChannel("channel", sender, Visibility.PUBLIC)

            val invitation =
                JdbiInvitationRepository(handle).createChannelInvitation(
                    sender,
                    receiver,
                    channel,
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                )

            invitation.markAsUsed()

            val updatedInvitation = JdbiInvitationRepository(handle).updateChannelInvitation(invitation)
            assertTrue(updatedInvitation.isUsed)
        }
    }

    @Test
    fun `delete register invitation by id should succeed`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")
            JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel", sender, Visibility.PUBLIC)

            val invitation =
                JdbiInvitationRepository(handle).createRegisterInvitation(
                    sender,
                    "receiver@test.com",
                    channel,
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                    "code",
                )

            val deleted = JdbiInvitationRepository(handle).deleteRegisterInvitationById(invitation.id)
            assertTrue(deleted)

            val foundInvitation = JdbiInvitationRepository(handle).findRegisterInvitationById(invitation.id)
            assertNull(foundInvitation)
        }
    }

    @Test
    fun `delete register invitation by id should return null for non existing invitation`() {
        runWithHandle { handle ->
            val deleted = JdbiInvitationRepository(handle).deleteRegisterInvitationById(99)
            assertFalse(deleted)
        }
    }

    @Test
    fun `delete channel invitation by id should succeed`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")

            val receiver = JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel", sender, Visibility.PUBLIC)

            val invitation =
                JdbiInvitationRepository(handle).createChannelInvitation(
                    sender,
                    receiver,
                    channel,
                    Role.READ_WRITE,
                    LocalDateTime.now(),
                )

            val deleted = JdbiInvitationRepository(handle).deleteChannelInvitationById(invitation.id)
            assertTrue(deleted)

            val foundInvitation = JdbiInvitationRepository(handle).findChannelInvitationById(invitation.id)
            assertNull(foundInvitation)
        }
    }

    @Test
    fun `delete channel invitation by id should return null for non existing invitation`() {
        runWithHandle { handle ->
            val deleted = JdbiInvitationRepository(handle).deleteChannelInvitationById(99)
            assertFalse(deleted)
        }
    }

    @Test
    fun `get invitations of user should succeed`() {
        runWithHandle { handle ->
            val sender = JdbiUserRepository(handle).createUser("sender", "sender@test.com", "password")
            val sender2 = JdbiUserRepository(handle).createUser("sender2", "sender2@test.com", "password")
            val receiver = JdbiUserRepository(handle).createUser("receiver", "receiver@test.com", "password")
            val channel = JdbiChannelRepository(handle).createChannel("channel", sender, Visibility.PUBLIC)

            JdbiInvitationRepository(handle).createChannelInvitation(
                sender,
                receiver,
                channel,
                Role.READ_WRITE,
                LocalDateTime.now(),
            )

            JdbiInvitationRepository(handle).createChannelInvitation(
                sender2,
                receiver,
                channel,
                Role.READ_WRITE,
                LocalDateTime.now(),
            )

            val invitations = JdbiInvitationRepository(handle).getInvitationsOfUser(receiver)
            assertEquals(invitations.size, 2)
        }
    }
}
