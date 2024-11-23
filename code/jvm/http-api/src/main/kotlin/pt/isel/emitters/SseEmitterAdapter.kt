package pt.isel.emitters

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.SseEvent
import pt.isel.UpdateEmitter

class SseEmitterAdapter(
    private val sseEmitter: SseEmitter,
) : UpdateEmitter {
    override fun emit(signal: SseEvent) {
        val msg =
            when (signal) {
                is SseEvent.NewChannelMessage ->
                    SseEmitter
                        .event()
                        .id(signal.id.toString())
                        .name("NewChannelMessage")
                        .data(signal)
                is SseEvent.ChannelNameUpdate ->
                    SseEmitter
                        .event()
                        .id(signal.id.toString())
                        .name("ChannelNameUpdate")
                        .data(signal.channel)
                is SseEvent.ChannelNewMemberUpdate ->
                    SseEmitter
                        .event()
                        .id(signal.id.toString())
                        .name("ChannelNewMemberUpdate")
                        .data(signal.newMember)
                is SseEvent.ChannelMemberExitedUpdate ->
                    SseEmitter.event()
                        .id(signal.id.toString())
                        .name("ChannelMemberExitedUpdate")
                        .data(signal.removedMember)
                is SseEvent.NewInvitationUpdate ->
                    SseEmitter.event()
                        .id(signal.id.toString())
                        .name("NewInvitationUpdate")
                        .data(signal.invitation)
                is SseEvent.InvitationAcceptedUpdate ->
                    SseEmitter.event()
                        .id(signal.id.toString())
                        .name("InvitationAcceptedUpdate")
                        .data(signal.invitation)
                is SseEvent.KeepAlive -> SseEmitter.event().comment(signal.timestamp.epochSeconds.toString())
            }
        sseEmitter.send(msg)
    }

    override fun onCompletion(callback: () -> Unit) {
        sseEmitter.onCompletion(callback)
    }

    override fun onError(callback: (Throwable) -> Unit) {
        sseEmitter.onError(callback)
    }
}
