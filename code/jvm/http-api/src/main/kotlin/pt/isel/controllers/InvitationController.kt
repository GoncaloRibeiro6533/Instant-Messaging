@file:Suppress("ktlint")
package pt.isel.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.AuthenticatedUser
import pt.isel.Failure
import pt.isel.InvitationError
import pt.isel.InvitationService
import pt.isel.Success
import pt.isel.models.Problem
import pt.isel.models.channel.ChannelOutputModel
import pt.isel.models.invitation.*
import pt.isel.models.user.UserIdentifiers

@RestController
@RequestMapping("api/invitation")
class InvitationController(
    private val invitationService: InvitationService,
) {
    @PostMapping("/register")
    fun createRegisterInvitation(
        @RequestBody invitationInputModelRegister: InvitationInputModelRegister,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result =
            invitationService.createRegisterInvitation(
                user.user.id,
                invitationInputModelRegister.email,
                invitationInputModelRegister.channelId,
                invitationInputModelRegister.role,
            )

        return when (result) {
            is Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(
                    InvitationOutputModelRegister(
                        result.value.id,
                        UserIdentifiers(
                            result.value.sender.id,
                            result.value.sender.username,
                        ),
                        result.value.email,
                        ChannelOutputModel(
                            result.value.channel.id,
                            result.value.channel.name,
                            UserIdentifiers(
                                result.value.channel.creator.id,
                                result.value.channel.creator.username,
                            ),
                            result.value.channel.visibility,
                        ),
                        result.value.role,
                        result.value.timestamp,
                    ),
                )
            is Failure ->
                handleInvitationError(result.value)
        }
    }

    @PostMapping("/channel")
    fun createChannelInvitation(
        @RequestBody invitationInputModelChannel: InvitationInputModelChannel,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result =
            invitationInputModelChannel.channelId.let {
                invitationService.createChannelInvitation(
                    user.user.id,
                    invitationInputModelChannel.receiverId,
                    invitationInputModelChannel.channelId,
                    invitationInputModelChannel.role,
                )
            }

        return when (result) {
            is Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(
                    InvitationOutputModelChannel(
                        result.value.id,
                        UserIdentifiers(
                            result.value.sender.id,
                            result.value.sender.username,
                        ),
                        UserIdentifiers(
                            result.value.receiver.id,
                            result.value.receiver.username,
                        ),
                        ChannelOutputModel(
                            result.value.channel.id,
                            result.value.channel.name,
                            UserIdentifiers(
                                result.value.channel.creator.id,
                                result.value.channel.creator.username,
                            ),
                            result.value.channel.visibility,
                        ),
                        result.value.role,
                        result.value.timestamp,
                    ),
                )
            is Failure ->
                handleInvitationError(result.value)
        }
    }

    @PutMapping("/accept/{invitationId}")
    fun acceptChannelInvitation(
        @PathVariable invitationId: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result =
            invitationService.acceptChannelInvitation(
                invitationId,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure ->
                handleInvitationError(result.value)
        }
    }

    @PutMapping("/decline/{invitationId}")
    fun declineInvitation(
        @PathVariable invitationId: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result =
            invitationService.declineChannelInvitation(
                invitationId,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.ACCEPTED).body(result.value)
            is Failure ->
                handleInvitationError(result.value)
        }
    }

    @GetMapping("user/invitations")
    fun getInvitations(user: AuthenticatedUser): ResponseEntity<*> {
        val result =
            invitationService.getInvitationsOfUser(
                user.user.id,
            )
        return when (result) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    InvitationsList(
                        result.value.size,
                        result.value.map {
                            InvitationOutputModelChannel(
                                it.id,
                                UserIdentifiers(it.sender.id, it.sender.username),
                                UserIdentifiers(it.receiver.id, it.receiver.username),
                                ChannelOutputModel(
                                    it.channel.id,
                                    it.channel.name,
                                    UserIdentifiers(it.channel.creator.id, it.channel.creator.username),
                                    it.channel.visibility,
                                ),
                                it.role,
                                it.timestamp,
                            )
                        },
                    ),
                )
            is Failure -> handleInvitationError(result.value)
        }
    }

    fun handleInvitationError(error: InvitationError): ResponseEntity<*> {
        return when (error) {
            is InvitationError.InvitationNotFound -> Problem.InvitationNotFound.response(HttpStatus.UNAUTHORIZED)
            is InvitationError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
            is InvitationError.InvalidRole -> Problem.InvalidRole.response(HttpStatus.BAD_REQUEST)
            is InvitationError.InvalidChannel -> Problem.InvalidChannel.response(HttpStatus.BAD_REQUEST)
            is InvitationError.NegativeIdentifier -> Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST)
            is InvitationError.InvitationExpired -> Problem.InvitationExpired.response(HttpStatus.BAD_REQUEST)
            is InvitationError.InvalidReceiver -> Problem.InvalidReceiver.response(HttpStatus.BAD_REQUEST)
            is InvitationError.Unauthorized -> Problem.Unauthorized.response(HttpStatus.UNAUTHORIZED)
            is InvitationError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
            is InvitationError.SenderDoesntBelongToChannel -> Problem.SenderDoesntBelongToChannel.response(HttpStatus.BAD_REQUEST)
            is InvitationError.AlreadyInChannel -> Problem.AlreadyInChannel.response(HttpStatus.BAD_REQUEST)
            is InvitationError.InvitationAlreadyUsed -> Problem.InvitationAlreadyUsed.response(HttpStatus.BAD_REQUEST)
            is InvitationError.CantInviteToPublicChannel -> Problem.CantInviteToPublicChannel.response(HttpStatus.BAD_REQUEST)
            is InvitationError.ChannelNotFound -> Problem.ChannelNotFound.response(HttpStatus.NOT_FOUND)
        }
    }
}
