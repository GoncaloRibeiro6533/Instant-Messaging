package pt.isel.talkRooms
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import pt.isel.AuthenticatedUser
import pt.isel.Channel
import pt.isel.ChannelInvitation
import pt.isel.Emitter
import pt.isel.Message
import pt.isel.NewMember
import pt.isel.RemovedMember
import pt.isel.Role
import pt.isel.SseEvent
import pt.isel.TransactionManager
import pt.isel.UpdateEmitter
import pt.isel.User
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TestEmitter(
    private val trxManager: TransactionManager,
): Emitter {
    // Important: mutable state on a singleton service
    private val listeners = mutableMapOf<AuthenticatedUser, UpdateEmitter>()
    private var currentId = 0L
    private val lock = ReentrantLock()

    // A scheduler to send the periodic keep-alive events
    private val scheduler: ScheduledExecutorService =
        Executors.newScheduledThreadPool(1).also {
            it.scheduleAtFixedRate({ keepAlive() }, 2, 2, TimeUnit.SECONDS)
        }
    override fun shutdown() {
        scheduler.shutdown()
    }

    override fun addEmitter(
        listener: UpdateEmitter,
        authenticatedUser: AuthenticatedUser,
    ) = lock.withLock {
        val user =
            trxManager.run {
                userRepo.findById(authenticatedUser.user.id)
            }
        requireNotNull(user)
        val oldListener = listeners.getOrDefault(authenticatedUser, listener)
        val newListener = if (oldListener == listener) oldListener else listener
        listeners[authenticatedUser] = newListener
        listener.onCompletion {
            removeEmitter(authenticatedUser)
        }
        listener.onError {
            removeEmitter(authenticatedUser)
        }
        listener
    }

    private fun removeEmitter(authenticatedUser: AuthenticatedUser) =
        lock.withLock {
            val oldListener = listeners[authenticatedUser]
            requireNotNull(oldListener)
            listeners.remove(authenticatedUser)
        }

    private fun keepAlive() =
        lock.withLock {
            val signal = SseEvent.KeepAlive(Clock.System.now())
            listeners.values.forEach {
                try {
                    it.emit(signal)
                } catch (ex: Exception) {
                }
            }
        }

    private fun sendEventToAll(
        users: Set<User>,
        signal: SseEvent,
    ) {
        val ids = users.map{ it.id}
        listeners.filter { it -> it.key.user.id in ids }.values.forEach {
            try {
                it.emit(signal)
            } catch (ex: Exception) {
            }
        }
    }

    override fun sendEventOfNewMember(
        channel: Channel,
        userToAddInfo: User,
        role: Role,
        users: Set<User>,
    ) {
        sendEventToAll(users, SseEvent.ChannelNewMemberUpdate(currentId++, NewMember(channel, userToAddInfo, role)))
    }

    override fun sendEventOfNewMessage(
        message: Message,
        users: Set<User>,
    ) = sendEventToAll(users, SseEvent.NewChannelMessage(currentId++, message))

    override fun sendEventOfChannelNameUpdated(
        updatedChannel: Channel,
        users: Set<User>,
    ) = sendEventToAll(users, SseEvent.ChannelNameUpdate(currentId++, updatedChannel))

    override fun sendEventOfMemberExited(
        channel: Channel,
        user: User,
        users: Set<User>,
    ) = sendEventToAll(users, SseEvent.ChannelMemberExitedUpdate(currentId++, RemovedMember(channel, user)))

    override fun sendEventOfNewInvitation(invitation: ChannelInvitation) =
        sendEventToAll(
            setOf(invitation.receiver),
            SseEvent.NewInvitationUpdate(currentId++, invitation),
        )

    override fun sendEventOfInvitationAccepted(invitation: ChannelInvitation) =
        sendEventToAll(
            setOf(invitation.receiver),
            SseEvent.InvitationAcceptedUpdate(currentId++, invitation),
        )

    override fun sendEventOfUsernameUpdate(users: Set<User>, newMember: User){
        sendEventToAll(users,
            SseEvent.MemberUsernameUpdate(currentId++, newMember)
            )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TestEmitter::class.java)
    }
}
