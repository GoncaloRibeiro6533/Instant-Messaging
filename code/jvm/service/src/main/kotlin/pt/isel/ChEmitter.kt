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
class ChEmitter(private val trxManager: TransactionManager) {
    init {
        logger.info("ChEmitter created")
    }

    // Important: mutable state on a singleton service
    private val listeners = mutableMapOf<Channel, List<ChannelUpdateEmitter>>()
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
        channelId: Int,
        listener: ChannelUpdateEmitter,
    ) = lock.withLock {
        val ch =
            trxManager.run {
                channelRepo.findById(channelId)
            }
        requireNotNull(ch)
        logger.info("adding listener")
        val oldListeners = listeners.getOrDefault(ch, emptyList())
        val newListener = oldListeners.find { it == listener } ?: listener
        // listeners.putIfAbsent(ch, oldListeners + listener)
        listeners[ch] = oldListeners + newListener
        logger.info("listeners: {}", listeners)
        listener.onCompletion {
            logger.info("onCompletion")
            removeEmitter(ch, listener)
        }
        listener.onError {
            logger.info("onError")
            removeEmitter(ch, listener)
        }
        listener
    }

    private fun removeEmitter(
        ch: Channel,
        listener: ChannelUpdateEmitter,
    ) = lock.withLock {
        logger.info("removing listener")
        val oldListeners = listeners[ch]
        requireNotNull(oldListeners)
        listeners.putIfAbsent(ch, oldListeners - listener)
    }

    private fun keepAlive() =
        lock.withLock {
            logger.info("keepAlive, sending to {} listeners", listeners.values.flatten().size)
            val signal = ChannelUpdate.KeepAlive(Clock.System.now())
            listeners.values.flatten().forEach {
                try {
                    it.emit(signal)
                } catch (ex: Exception) {
                    logger.info("Exception while sending keepAlive signal - {}", ex.message)
                }
            }
        }

    private fun sendEventToAll(
        ch: Channel,
        signal: ChannelUpdate,
    ) {
        listeners[ch]?.forEach {
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
    ) {
        sendEventToAll(channel, ChannelUpdate.ChannelNewMemberUpdate(currentId++, channel, userToAddInfo, role))
    }

    fun sendEventOfNewMessage(
        channel: Channel,
        message: Message,
    ) = sendEventToAll(channel, ChannelUpdate.NewChannelMessage(currentId++, message))

    fun sendEventOfChannelNameUpdated(
        channel: Channel,
        updatedChannel: Channel,
    ) = sendEventToAll(channel, ChannelUpdate.ChannelNameUpdate(currentId++, updatedChannel))

    fun sendEventOfMemberExited(
        channel: Channel,
        user: User,
    ) = sendEventToAll(channel, ChannelUpdate.ChannelMemberExitedUpdate(currentId++, channel, user))

    companion object {
        private val logger = LoggerFactory.getLogger(ChEmitter::class.java)
    }
}
