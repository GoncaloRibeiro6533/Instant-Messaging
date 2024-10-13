package controllers

import Failure
import InvitationService
import java.security.Principal
import models.InvitationInputModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import Success

@RestController
@RequestMapping("api/invitation")
class InvitationController(private val invitationService: InvitationService) {
    @PostMapping("/channel")
    fun createChannelInvitation(
        @RequestBody invitationInputModel: InvitationInputModel,
        @RequestHeader("Authorization") token: String,
        principal: Principal,
    ): ResponseEntity<Any> {
        // Obtenha o ID do usuário autenticado a partir do principal
        val senderId = principal.name.toInt()

        val result =
            invitationInputModel.channelId?.let {
                invitationService.createChannelInvitation(
                    senderId,
                    invitationInputModel.receiverId,
                    invitationInputModel.channelId,
                    invitationInputModel.role.toString(),
                    token,
                )
            }

        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> -> ResponseEntity.badRequest().body(result.value)
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
        // Obtenha o ID do usuário autenticado a partir do principal
        val senderId = principal.name.toInt()

        val result =
            invitationInputModel.email?.let {
                invitationService.createRegisterInvitation(
                    senderId,
                    invitationInputModel.email,
                    invitationInputModel.channelId,
                    invitationInputModel.role,
                    token,
                )
            }

        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> -> ResponseEntity.badRequest().body(result.value)
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
            is Failure<*> -> ResponseEntity.badRequest().body(result.value)
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
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
            is Failure<*> -> ResponseEntity.badRequest().body(result.value)
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }

    @GetMapping("/register")
    fun getChannelInvitations(
        @RequestParam invitationId: Int,
    ): ResponseEntity<Any> {
        val result =
            invitationService.getRegisterInvitationById(
                invitationId,
            )

        return when (result) {
            is Success<*> -> ResponseEntity.ok(result.value)
            is Failure<*> -> ResponseEntity.badRequest().body(result.value)
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }
}
