package pt.isel
import jakarta.annotation.PreDestroy
import jakarta.inject.Named
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Named
class UpdatesEmitter(
    private val trxManager: TransactionManager,
) {
    // Important: mutable state on a singleton service
    private val listeners = mutableMapOf<AuthenticatedUser, UpdateEmitter>()
    private var currentId = 0L
    private val lock = ReentrantLock()

    // A scheduler to send the periodic keep-alive events
    private val scheduler: ScheduledExecutorService =
        Executors.newScheduledThreadPool(1).also {
            it.scheduleAtFixedRate({ keepAlive() }, 2, 2, TimeUnit.SECONDS)
        }

    @PreDestroy
    fun shutdown() {
        logger.info("shutting down")
        scheduler.shutdown()
    }

    fun addEmitter(
        listener: UpdateEmitter,
        authenticatedUser: AuthenticatedUser,
    ) = lock.withLock {
        val user =
            trxManager.run {
                userRepo.findById(authenticatedUser.user.id)
            }
        requireNotNull(user)
        logger.info("adding listener")
        val oldListener = listeners.getOrDefault(authenticatedUser, listener)
        val newListener = if (oldListener == listener) oldListener else listener
        listeners[authenticatedUser] = newListener
        logger.info("listeners: {}", listeners)
        listener.onCompletion {
            logger.info("onCompletion")
            removeEmitter(authenticatedUser)
        }
        listener.onError {
            logger.info("onError")
            removeEmitter(authenticatedUser)
        }
        listener
    }

    private fun removeEmitter(authenticatedUser: AuthenticatedUser) =
        lock.withLock {
            logger.info("removing listener")
            val oldListener = listeners[authenticatedUser]
            requireNotNull(oldListener)
            listeners.remove(authenticatedUser)
        }

    private fun keepAlive() =
        lock.withLock {
            // logger.info("keepAlive, sending to {} listeners", listeners.values.size)
            val signal = SseEvent.KeepAlive(Clock.System.now())
            listeners.values.forEach {
                try {
                    it.emit(signal)
                } catch (ex: Exception) {
                    logger.info("Exception while sending keepAlive signal - {}", ex.message)
                }
            }
        }

    private fun sendEventToAll(
        users: Set<User>,
        signal: SseEvent,
    ) {
        listeners.filter { it.key.user in users }.values.forEach {
            try {
                it.emit(signal)
            } catch (ex: Exception) {
                logger.info("Exception while sending Message signal - {}", ex.message)
            }
        }
    }

    fun sendEventOfNewMember(
        channel: Channel,
        userToAddInfo: User,
        role: Role,
        users: Set<User>,
    ) {
        sendEventToAll(users, SseEvent.ChannelNewMemberUpdate(currentId++, NewMember(channel, userToAddInfo, role)))
    }

    fun sendEventOfNewMessage(
        message: Message,
        users: Set<User>,
    ) = sendEventToAll(users, SseEvent.NewChannelMessage(currentId++, message))

    fun sendEventOfChannelNameUpdated(
        updatedChannel: Channel,
        users: Set<User>,
    ) = sendEventToAll(users, SseEvent.ChannelNameUpdate(currentId++, updatedChannel))

    fun sendEventOfMemberExited(
        channel: Channel,
        user: User,
        users: Set<User>,
    ) = sendEventToAll(users, SseEvent.ChannelMemberExitedUpdate(currentId++, RemovedMember(channel, user)))

    fun sendEventOfNewInvitation(invitation: ChannelInvitation) =
        sendEventToAll(
            setOf(invitation.receiver),
            SseEvent.NewInvitationUpdate(currentId++, invitation),
        )

    fun sendEventOfInvitationAccepted(invitation: ChannelInvitation) =
        sendEventToAll(
            setOf(invitation.sender, invitation.receiver),
            SseEvent.InvitationAcceptedUpdate(currentId++, invitation),
        )

    companion object {
        private val logger = LoggerFactory.getLogger(UpdatesEmitter::class.java)
    }
}
