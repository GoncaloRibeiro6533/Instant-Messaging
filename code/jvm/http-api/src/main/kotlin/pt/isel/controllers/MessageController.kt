package pt.isel.controllers

import org.springframework.http.HttpStatus
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
import pt.isel.models.Problem

@RestController
@RequestMapping("api/messages")
class MessageController(private val messageService: MessageService) {
    @GetMapping("/{id}")
    fun getMessageById(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result =
            messageService.findMessageById(
                id,
                user.user.id,
            )

        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure ->
                handleMessageError(result.value)
        }
    }

    @PostMapping
    fun sendMessage(
        @RequestBody messageInputModel: MessageInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result =
            messageService.sendMessage(
                messageInputModel.channelId,
                user.user.id,
                messageInputModel.content
            )

        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
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
    ): ResponseEntity<*> {
        val result =
            messageService.getMsgHistory(
                channelId,
                limit,
                skip,
                user.user.id,
            )

        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure ->
                handleMessageError(result.value)
        }
    }

    fun handleMessageError(error: MessageError): ResponseEntity<*> {
        return when (error) {
            is MessageError.MessageNotFound -> Problem.MessageNotFound.response(HttpStatus.NOT_FOUND)
            is MessageError.InvalidChannelId -> Problem.InvalidIdentifier.response(HttpStatus.BAD_REQUEST)
            is MessageError.InvalidText -> Problem.InvalidText.response(HttpStatus.BAD_REQUEST)
            is MessageError.InvalidLimit -> Problem.NegativeLimit.response(HttpStatus.BAD_REQUEST)
            is MessageError.InvalidSkip -> Problem.NegativeSkip.response(HttpStatus.BAD_REQUEST)
            is MessageError.NegativeIdentifier -> Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST)
            is MessageError.Unauthorized -> Problem.Unauthorized.response(HttpStatus.UNAUTHORIZED)
            is MessageError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
            is MessageError.InvalidUserId -> Problem.InvalidIdentifier.response(HttpStatus.BAD_REQUEST)
            is MessageError.ChannelNotFound -> Problem.ChannelNotFound.response(HttpStatus.NOT_FOUND)
            is MessageError.UserNotInChannel -> Problem.UserNotInChannel.response(HttpStatus.BAD_REQUEST)
        }
    }
}
