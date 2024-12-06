package pt.isel.emitters

import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.AuthenticatedUser
import pt.isel.UpdatesEmitter
import pt.isel.UserService
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/sse")
class SseController(
    private val emitter: UpdatesEmitter,
    private val userService: UserService,
) {
    @GetMapping("/listen/{token}")
    fun listen(
        @PathVariable token: String,
        //user: AuthenticatedUser
        ): SseEmitter {
        val user = userService.getUserByToken(token) ?: throw Exception("User not found") //TODO
        val sseEmitter =
            SseEmitter(
                TimeUnit.HOURS.toMillis(1),
            )
        emitter.addEmitter(
            SseEmitterAdapter(
                sseEmitter,
            ), AuthenticatedUser(user, token)
                    )
        return sseEmitter
    }
}
