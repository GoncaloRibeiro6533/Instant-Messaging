package controllers

import AuthenticatedUser
import Failure
import InvitationError
import InvitationService
import Success
import models.invitation.InvitationInputModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/invitation")
class InvitationController(private val invitationService: InvitationService) {
    @PostMapping("/channel")
    fun createChannelInvitation(
        @RequestBody invitationInputModel: InvitationInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        val result =
            invitationInputModel.channelId.let {
                invitationService.createChannelInvitation(
                    user.user.id,
                    invitationInputModel.receiverId,
                    invitationInputModel.channelId,
                    invitationInputModel.role,
                )
            }

        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> ->
                handleInvitationError(result.value)
        }
    }

    @PostMapping("/register")
    fun createRegisterInvitation(
        @RequestBody invitationInputModel: InvitationInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        val result =
            invitationInputModel.email.let {
                invitationService.createRegisterInvitation(
                    user.user.id,
                    invitationInputModel.email,
                    invitationInputModel.channelId,
                    invitationInputModel.role,
                )
            }
        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> ->
                handleInvitationError(result.value)
        }
    }

    @PostMapping("/accept")
    fun acceptRegisterInvitation(
        @RequestParam invitationId: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        val result =
            invitationService.acceptChannelInvitation(
                invitationId,
            )
        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> ->
                handleInvitationError(result.value)
        }
    }

    @GetMapping("")
    fun getInvitations(user: AuthenticatedUser): ResponseEntity<Any> {
        val result =
            invitationService.getInvitationsOfUser(
                user.user.id,
            )
        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> ->
                handleInvitationError(result.value)
        }
    }

    @GetMapping("/register")
    fun getRegisterInvitationById(
        @RequestParam invitationId: Int,
    ): ResponseEntity<Any> {
        val result =
            invitationService.getRegisterInvitationById(
                invitationId,
            )
        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> ->
                handleInvitationError(result.value)
        }
    }

    @PutMapping("/decline")
    fun declineInvitation(
        @RequestParam invitationId: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        val result =
            invitationService.declineChannelInvitation(
                invitationId,
            )
        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> ->
                handleInvitationError(result.value)
        }
    }

    fun handleInvitationError(error: Any?): ResponseEntity<Any> {
        return when (error) {
            is InvitationError.InvitationNotFound -> ResponseEntity.notFound().build()
            is InvitationError.InvalidEmail -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.InvalidRole -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.NegativeIdentifier -> ResponseEntity.badRequest().body(error)
            is InvitationError.InvalidReceiver -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.InvitationExpired -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.Unauthorized -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.UserNotFound -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.SenderDoesntBelongToChannel -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.AlreadyInChannel -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.InvitationAlreadyUsed -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.CantInviteToPublicChannel -> ResponseEntity.unprocessableEntity().body(error)
            is InvitationError.ChannelNotFound -> ResponseEntity.unprocessableEntity().body(error)
            else -> ResponseEntity.internalServerError().body("Internal server error")
        }
    }
}
