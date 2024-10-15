package controllers

import Failure
import Success
import UserService
import models.UserInputModel
import models.UserOutputModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UsersController(
    private val userService: UserService,
) {
    @PostMapping("api/users/register")
    fun registerFirstUser(
        @RequestBody body: UserInputModel,
    ): ResponseEntity<Any> {
        return when (
            val result =
                userService.addFirstUser(
                    body.username,
                    body.email,
                    body.password,
                )
        ) {
            is Success -> {
                val outputModel =
                    UserOutputModel(
                        result.value.id,
                        result.value.username,
                        result.value.email,
                    )
                ResponseEntity
                    .status(201)
                    .body(outputModel)
            }

            is Failure ->
                when (result.value) {
                    is UserError.NotFirstUser -> ResponseEntity.badRequest().body(result.value)
                    is UserError.InvalidUsername -> ResponseEntity.badRequest().body(result.value)
                    is UserError.InvalidPassword -> ResponseEntity.badRequest().body(result.value)
                    is UserError.UsernameToLong -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.badRequest().body("Unknown error")
            }
        }
    }

    @GetMapping("api/users/{id}")
    fun getUserById(
        @PathVariable id: Int,
        @RequestParam token: String,
    ): ResponseEntity<Any> {
        return when (val result = userService.getUserById(id, token)) {
            is Success -> {
                val outputModel =
                    UserOutputModel(
                        result.value.id,
                        result.value.username,
                        result.value.email,
                    )
                ResponseEntity.ok(outputModel)
            }
            is Failure ->
                when (result.value) {
                    is UserError.Unauthorized -> ResponseEntity.badRequest().body(result.value)
                    is UserError.SessionExpired -> ResponseEntity.badRequest().body(result.value)
                    is UserError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    is UserError.UserNotFound -> ResponseEntity.notFound().build()
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.badRequest().body("Unknown error")
            }
        }
    }

    @GetMapping("api/users/{name}")
    fun findUserByUsername(
        @PathVariable name: String,
        @RequestParam token: String,
        @RequestParam limit: Int,
        @RequestParam skip: Int,
    ): ResponseEntity<Any> {
        return when (val result = userService.findUserByUsername(name, token, limit, skip)) {
            is Success -> {
                val outputModel =
                    result.value.map {
                        UserOutputModel(
                            it.id,
                            it.username,
                            it.email,
                        )
                    }
                ResponseEntity.ok(outputModel)
            }
            is Failure ->
                when (result.value) {
                    is UserError.Unauthorized -> ResponseEntity.badRequest().body(result.value)
                    is UserError.InvalidUsername -> ResponseEntity.badRequest().body(result.value)
                    is UserError.SessionExpired -> ResponseEntity.badRequest().body(result.value)
                    is UserError.NegativeLimit -> ResponseEntity.badRequest().body(result.value)
                    is UserError.NegativeSkip -> ResponseEntity.badRequest().body(result.value)
                    is UserError.UserNotFound -> ResponseEntity.notFound().build() // TODO service retorna esta exceção?
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.badRequest().body("Unknown error")
            }
        }
    }
}
