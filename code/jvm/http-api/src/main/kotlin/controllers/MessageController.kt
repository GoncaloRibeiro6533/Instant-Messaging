package controllers

import Failure
import MessageService
import Success
import models.MessageInputModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/messages")
class MessageController(private val messageService: MessageService) {
    @GetMapping("/{id}")
    fun getMessageById(
        @PathVariable id: Int,
        @RequestHeader("Authorization") token: String,
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
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<Any> {
        val result =
            messageService.sendMessage(
                messageInputModel.channelId,
                messageInputModel.userId,
                messageInputModel.content,
                token,
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
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<Any> {
        val result = messageService.getMsgHistory(channelId, limit, skip, token)

        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> ResponseEntity.badRequest().body(result.value)
        }
    }
}
