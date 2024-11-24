@file:Suppress("ktlint")
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
import pt.isel.models.*
import pt.isel.models.channel.ChannelIdentifiers
import pt.isel.models.user.UserIdentifiers

@RestController
@RequestMapping("api/messages")
class MessageController(private val messageService: MessageService) {
    @PostMapping
    fun sendMessage(
        @RequestBody messageInputModel: MessageInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result =
            messageService.sendMessage(
                messageInputModel.channelId,
                user.user.id,
                messageInputModel.content,
            )

        return when (result) {
            is Success -> {
                val outputModel =
                    MessageOutputModel(
                        msgId = result.value.id,
                        sender = UserIdentifiers(
                            id = result.value.sender.id,
                            username = result.value.sender.username,
                            email = result.value.sender.email,
                        ),
                        channel = ChannelIdentifiers(
                            id = result.value.channel.id,
                            name = result.value.channel.name,
                        ),
                        content = result.value.content,
                        timestamp = result.value.timestamp,
                    )
                ResponseEntity.status(HttpStatus.CREATED).body(outputModel)
            }
            is Failure -> {
                handleMessageError(result.value)
            }
        }
    }

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
            is Success -> {
                val outputModel =
                    MessageOutputModel(
                        msgId = result.value.id,
                        sender = UserIdentifiers(
                            id = result.value.sender.id,
                            username = result.value.sender.username,
                            email = result.value.sender.email,
                        ),
                        channel = ChannelIdentifiers(
                            id = result.value.channel.id,
                            name = result.value.channel.name,
                        ),
                        content = result.value.content,
                        timestamp = result.value.timestamp,
                    )
                ResponseEntity.status(HttpStatus.OK).body(outputModel)
            }
            is Failure ->
                handleMessageError(result.value)
        }
    }

    @GetMapping("/history/{channelId}")
    fun getMsgHistory(
        @PathVariable channelId: Int,
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @RequestParam(required = false, defaultValue = "0") skip: Int,
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
            is Success -> {
                val outputModel =
                    MessageHistoryOutputModel(
                        channel = ChannelIdentifiers(
                            id = result.value[0].channel.id,
                            name = result.value[0].channel.name,
                        ),
                        messages =
                        result.value.map {
                            MessageInfoOutputModel(
                                msgId = it.id,
                                sender = UserIdentifiers(
                                    id = it.sender.id,
                                    username = it.sender.username,
                                    email = it.sender.email,
                                ),
                                content = it.content,
                                timestamp = it.timestamp
                            )
                        },
                        nrOfMessages = result.value.size,
                    )
                ResponseEntity.status(HttpStatus.OK).body(outputModel)
            }
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
