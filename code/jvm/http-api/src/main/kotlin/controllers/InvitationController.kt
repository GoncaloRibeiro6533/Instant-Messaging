package controllers

import AuthenticatedUser
import Failure
import InvitationError
import InvitationService
import Success
import models.invitation.InvitationInputModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
                when (result.value) {
                    // is InvitationError.InvalidReceiver -> ResponseEntity.badRequest().body(result.value)
                    // is InvitationError.NegativeIdentifier-> ResponseEntity.badRequest().body(result.value)
                    is InvitationError.Unauthorized -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.InvalidChannel -> ResponseEntity.badRequest().body(result.value)
                    is InvitationError.InvalidRole -> ResponseEntity.badRequest().body(result.value)
                    is InvitationError.SenderDoesntBelongToChannel -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.CantInviteToPublicChannel -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.AlreadyInChannel -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.UserNotFound -> ResponseEntity.notFound().build()

                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
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
                when (result.value) {
                    // is InvitationError.InvalidReceiver -> ResponseEntity.badRequest().body(result.value)
                    // is InvitationError.NegativeIdentifier-> ResponseEntity.badRequest().body(result.value)
                    // is InvitationError.InvalidEmail -> ResponseEntity.badRequest().body(result.value)
                    is InvitationError.Unauthorized -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.SenderDoesntBelongToChannel -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.CantInviteToPublicChannel -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.UserNotFound -> ResponseEntity.notFound().build()

                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
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
                when (result.value) {
                    // is InvitationError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    is InvitationError.Unauthorized -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.AlreadyInChannel -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.InvitationNotFound -> ResponseEntity.notFound().build()
                    is InvitationError.InvitationExpired -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.InvitationAlreadyUsed -> ResponseEntity.unprocessableEntity().body(result.value)
                    else -> {
                        ResponseEntity.badRequest().body(result.value)
                    }
                }
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
                when (result.value) {
                    // is InvitationError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    is InvitationError.Unauthorized -> ResponseEntity.unprocessableEntity().body(result.value)
                    is InvitationError.InvitationNotFound -> ResponseEntity.notFound().build()
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }

    @GetMapping("/register")
    fun getRegisterInvitation(
        @RequestParam invitationId: Int,
    ): ResponseEntity<Any> {
        val result =
            invitationService.getRegisterInvitationById(
                invitationId,
            )
        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> ->
                when (result.value) {
                    // is InvitationError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    is InvitationError.InvitationNotFound -> ResponseEntity.notFound().build()
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }
}
