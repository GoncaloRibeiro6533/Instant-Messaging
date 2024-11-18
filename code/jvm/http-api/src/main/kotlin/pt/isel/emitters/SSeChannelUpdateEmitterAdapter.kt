package pt.isel.emitters

import org.slf4j.LoggerFactory
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.ChannelUpdate
import pt.isel.ChannelUpdateEmitter

/*
 * - SseEmitter - Spring MVC type
 * - UpdatedTimeSlotEmitter is our own interface (domain)
 * - SseUpdatedTimeSlotEmitterAdapter is our own type (http) that is adapted to the UpdatedTimeSlotEmitter interface,
 * -  which uses SseEmitter
 * Yuml class diagram:
   [TimeSlotController]->1[EventService]
   [TimeSlotController]-.-new>[SseUpdatedTimeSlotEmitterAdapter]
   [TimeSlotController]-.-new>[SseEmitter]
   [SseUpdatedTimeSlotEmitterAdapter]-.-new>[SseEventBuilder]
   [EventService]->*[UpdatedTimeSlotEmitter]
   [Message]-^[UpdatedTimeSlot]
   [KeepAlive]-^[UpdatedTimeSlot]
   [EventService]-.-new>[Message]
   [EventService]-.-new>[KeepAlive]
   [SseUpdatedTimeSlotEmitterAdapter]-^[UpdatedTimeSlotEmitter]
   [UpdatedTimeSlot]
   [Message|id: Long;slot: TimeSlot]
   [KeepAlive|timestamp: Instant]
   [UpdatedTimeSlotEmitter|emit(signal: UpdatedTimeSlot);onCompletion(callback: () -\> Unit);onError(callback: (Throwable) -\> Unit)]
   [EventService|addEmitter(eventId: Int, emitter: UpdatedTimeSlotEmitter);removeEmitter(eventId: Int, emitter: UpdatedTimeSlotEmitter);sendEventToAll(ev: Event, signal: UpdatedTimeSlot)]
 */

class SSeChannelUpdateEmitterAdapter(
    private val sseEmitter: SseEmitter,
) : ChannelUpdateEmitter {
    init {
        logger.info("SseEmitter created: {}", sseEmitter)
    }

    override fun emit(signal: ChannelUpdate) {
        val msg =
            when (signal) {
                is ChannelUpdate.NewChannelMessage ->
                    SseEmitter
                        .event()
                        .id(signal.id.toString())
                        .name("NewChannelMessage")
                        .data(signal.message)
                is ChannelUpdate.ChannelNameUpdate ->
                    SseEmitter
                        .event()
                        .id(signal.id.toString())
                        .name("ChannelNameUpdate")
                        .data(signal.channel)
                is ChannelUpdate.ChannelNewMemberUpdate ->
                    SseEmitter
                        .event()
                        .id(signal.id.toString())
                        .name("ChannelNewMemberUpdate")
                        .data(signal.channel)
                        .data(signal.newMember)
                        .data(signal.role)
                is ChannelUpdate.ChannelMemberExitedUpdate ->
                    SseEmitter.event()
                        .id(signal.id.toString())
                        .name("ChannelMemberExitedUpdate")
                        .data(signal.channel)
                        .data(signal.removedMember)
                is ChannelUpdate.KeepAlive -> SseEmitter.event().comment(signal.timestamp.epochSeconds.toString())
            }
        sseEmitter.send(msg)
    }

    override fun onCompletion(callback: () -> Unit) {
        sseEmitter.onCompletion(callback)
    }

    override fun onError(callback: (Throwable) -> Unit) {
        sseEmitter.onError(callback)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SSeChannelUpdateEmitterAdapter::class.java)
    }
}
