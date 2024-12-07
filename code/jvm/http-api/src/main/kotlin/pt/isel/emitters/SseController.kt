package pt.isel.emitters

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.AuthenticatedUser
import pt.isel.UpdatesEmitter
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/sse")
class SseController(
    private val emitter: UpdatesEmitter,
) {
    @GetMapping("/listen")
    fun listen(user: AuthenticatedUser): Any {
        val sseEmitter =
            SseEmitter(
                TimeUnit.HOURS.toMillis(1),
            )
        emitter.addEmitter(
            SseEmitterAdapter(
                sseEmitter,
            ),
            user,
        )
        return sseEmitter
    }
}
