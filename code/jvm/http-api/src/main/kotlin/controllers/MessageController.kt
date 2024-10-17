package controllers

import AuthenticatedUser
import Failure
import MessageService
import Success
import models.MessageInputModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/messages")
class MessageController(private val messageService: MessageService) {
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
                when (result.value) {
                    is MessageError.MessageNotFound -> ResponseEntity.notFound().build()
                    is MessageError.UserNotInChannel -> ResponseEntity.unprocessableEntity().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }

            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }

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
                when (result.value) {
                    // is MessageError.InvalidChannelId -> ResponseEntity.badRequest().body(result.value)
                    // is MessageError.InvalidText -> ResponseEntity.badRequest().body(result.value)
                    // is MessageError.InvalidUserId -> ResponseEntity.badRequest().body(result.value)
                    is MessageError.Unauthorized -> ResponseEntity.unprocessableEntity().body(result.value)
                    is MessageError.ChannelNotFound -> ResponseEntity.notFound().build()
                    is MessageError.UserNotInChannel -> ResponseEntity.notFound().build()
                    else -> ResponseEntity.internalServerError().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
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
                when (result.value) {
                    // is MessageError.InvalidChannelId -> ResponseEntity.badRequest().body(result.value)
                    // is MessageError.InvalidLimit -> ResponseEntity.badRequest().body(result.value)
                    // is MessageError.InvalidSkip -> ResponseEntity.badRequest().body(result.value)
                    is MessageError.ChannelNotFound -> ResponseEntity.notFound().build()
                    is MessageError.Unauthorized -> ResponseEntity.unprocessableEntity().body(result.value)
                    else -> ResponseEntity.internalServerError().body(result.value)
                }
        }
    }
}
