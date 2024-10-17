package controllers

import Failure
import InvitationError
import InvitationService
import Success
import models.invitation.InvitationInputModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("api/invitation")
class InvitationController(private val invitationService: InvitationService) {
    @PostMapping("/channel")
    fun createChannelInvitation(
        @RequestBody invitationInputModel: InvitationInputModel,
        @RequestHeader("Authorization") token: String,
        principal: Principal,
    ): ResponseEntity<Any> {
        val senderId = principal.name.toInt()

        val result =
            invitationInputModel.channelId.let {
                invitationService.createChannelInvitation(
                    senderId,
                    invitationInputModel.receiverId,
                    invitationInputModel.channelId,
                    invitationInputModel.role,
                    token,
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
        @RequestHeader("Authorization") token: String,
        principal: Principal,
    ): ResponseEntity<Any> {
        val senderId = principal.name.toInt()

        val result =
            invitationInputModel.email.let {
                invitationService.createRegisterInvitation(
                    senderId,
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
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<Any> {
        val result =
            invitationService.acceptChannelInvitation(
                invitationId,
                token,
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
    fun getInvitations(
        @RequestHeader("Authorization") token: String,
        principal: Principal,
    ): ResponseEntity<Any> {
        val userId = principal.name.toInt()

        val result =
            invitationService.getInvitationsOfUser(
                userId,
                token,
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
