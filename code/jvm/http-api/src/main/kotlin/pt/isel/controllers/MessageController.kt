package pt.isel.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.AuthenticatedUser
import pt.isel.Failure
import pt.isel.MessageError
import pt.isel.MessageService
import pt.isel.Success
import pt.isel.models.MessageInputModel

@RestController
@RequestMapping("api/messages")
class MessageController(private val messageService: MessageService) {
    @PostMapping
    fun sendMessage(
        @RequestBody messageInputModel: MessageInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        val result =
            messageService.sendMessage(
                messageInputModel.channelId,
                user.user.id,
                messageInputModel.content,
            )

        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> ->
                handleMessageError(result.value)
        }
    }

    @GetMapping("/{id}")
    fun getMessageById(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        val result =
            messageService.findMessageById(
                id,
                user.user.id,
            )

        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                handleMessageError(result.value)
        }
    }

    @GetMapping("/{channelId}")
    fun getMsgHistory(
        @PathVariable channelId: Int,
        @RequestParam limit: Int,
        @RequestParam skip: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        val result =
            messageService.getMsgHistory(
                channelId,
                limit,
                skip,
                user.user.id,
            )

        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                handleMessageError(result.value)
        }
    }

    fun handleMessageError(error: Any?): ResponseEntity<Any> {
        return when (error) {
            is MessageError.MessageNotFound -> ResponseEntity.notFound().build()
            is MessageError.InvalidChannelId -> ResponseEntity.badRequest().body(error)
            is MessageError.InvalidText -> ResponseEntity.badRequest().body(error)
            is MessageError.InvalidLimit -> ResponseEntity.badRequest().body(error)
            is MessageError.InvalidSkip -> ResponseEntity.badRequest().body(error)
            is MessageError.NegativeIdentifier -> ResponseEntity.badRequest().body(error)
            is MessageError.Unauthorized -> ResponseEntity.unprocessableEntity().body(error)
            is MessageError.UserNotFound -> ResponseEntity.notFound().build()
            is MessageError.InvalidUserId -> ResponseEntity.badRequest().body(error)
            is MessageError.UserNotInChannel -> ResponseEntity.unprocessableEntity().body(error)
            else -> ResponseEntity.internalServerError().body(error)
        }
    }
}
