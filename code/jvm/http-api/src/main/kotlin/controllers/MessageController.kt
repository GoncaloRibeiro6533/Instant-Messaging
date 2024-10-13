package controllers

import Message
import MessageService
import Either
import Failure
import MessageError
import Success
import models.MessageInputModel
import models.MessageOutputModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/messages")
class MessageController(private val messageService: MessageService) {

    @GetMapping("/{id}")
    fun getMessageById(
        @PathVariable id: Int,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Any> {
        val result = messageService.findMessageById(id, token)

        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> ResponseEntity.badRequest().body(result.value)
        }
    }

    @PostMapping
    fun sendMessage(
        @RequestBody messageInputModel: MessageInputModel,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Any> {
        val result = messageService.sendMessage(
            messageInputModel.channelId,
            messageInputModel.userId,
            messageInputModel.content,
            token
        )

        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> -> ResponseEntity.badRequest().body(result.value)
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
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Any> {
        val result = messageService.getMsgHistory(channelId, limit, skip, token)

        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> ResponseEntity.badRequest().body(result.value)
        }
    }

}
