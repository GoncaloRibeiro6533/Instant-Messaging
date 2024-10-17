package controllers

import AuthenticatedUser
import Either
import Failure
import Success
import User
import UserError
import UserService
import models.Problem
import models.user.UserLoginCredentialsInput
import models.user.UserRegisterInput
import models.user.UsernameUpdateInput
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/user")
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/register")
    fun registerFirstUser(
        @RequestBody userRegisterInput: UserRegisterInput,
    ): ResponseEntity<*> {
        val result: Either<UserError, User> =
            userService.addFirstUser(
                userRegisterInput.username.trim(),
                userRegisterInput.email.trim(),
                userRegisterInput.password,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure ->
                when (result.value) {
                    is UserError.NotFirstUser -> Problem.NotFirstUser.response(HttpStatus.CONFLICT)
                    is UserError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
                    is UserError.EmailCannotBeBlank -> Problem.EmailCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.UsernameCannotBeBlank -> Problem.UsernameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.PasswordCannotBeBlank -> Problem.PasswordCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.WeakPassword -> Problem.WeakPassword.response(HttpStatus.BAD_REQUEST)
                    is UserError.UsernameToLong -> Problem.UsernameToLong.response(HttpStatus.BAD_REQUEST)
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.value)
                }
        }
    }

    @PostMapping("/register/{invitationId}")
    fun register(
        @RequestBody userRegisterInput: UserRegisterInput,
        @PathVariable invitationId: String,
    ): ResponseEntity<*> {
        val result: Either<UserError, User> =
            userService.createUser(
                userRegisterInput.username.trim(),
                userRegisterInput.email.trim(),
                userRegisterInput.password,
                invitationId.toInt(),
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure ->
                when (result.value) {
                    is UserError.NegativeIdentifier -> Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST)
                    is UserError.InvitationNotFound -> Problem.InvitationNotFound.response(HttpStatus.BAD_REQUEST)
                    is UserError.InvitationAlreadyUsed -> Problem.InvitationAlreadyUsed.response(HttpStatus.CONFLICT)
                    is UserError.UsernameCannotBeBlank -> Problem.UsernameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.PasswordCannotBeBlank -> Problem.PasswordCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.EmailCannotBeBlank -> Problem.EmailCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
                    is UserError.EmailAlreadyInUse -> Problem.EmailAlreadyInUse.response(HttpStatus.CONFLICT)
                    is UserError.EmailDoesNotMatchInvite -> Problem.EmailDoesNotMatchInvite.response(HttpStatus.BAD_REQUEST)
                    is UserError.UsernameToLong -> Problem.UsernameToLong.response(HttpStatus.BAD_REQUEST)
                    is UserError.UsernameAlreadyExists -> Problem.UsernameAlreadyInUse.response(HttpStatus.CONFLICT)
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.value)
                }
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestBody userLoginInput: UserLoginCredentialsInput,
    ): ResponseEntity<*> {
        val result: Either<UserError, AuthenticatedUser> =
            userService.loginUser(
                userLoginInput.username.trim(),
                userLoginInput.password,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure ->
                when (result.value) {
                    is UserError.UsernameCannotBeBlank -> Problem.UsernameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.PasswordCannotBeBlank -> Problem.PasswordCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.NoMatchingUsername -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    is UserError.NoMatchingPassword -> Problem.InvalidPassword.response(HttpStatus.UNAUTHORIZED)
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.value)
                }
        }
    }

    @PostMapping("/logout")
    fun logout(user: AuthenticatedUser): ResponseEntity<*> {
        val result: Either<UserError, Unit> = userService.logoutUser(user.token)
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(null)
            is Failure ->
                when (result.value) {
                    is UserError.SessionExpired -> Problem.SessionExpired.response(HttpStatus.UNAUTHORIZED)
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.value)
                }
        }
    }

    @PutMapping("/edit/username")
    fun editUsername(
        @RequestBody usernameUpdateInput: UsernameUpdateInput,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result: Either<UserError, User> =
            userService.updateUsername(
                user.user.id,
                usernameUpdateInput.newUsername.trim(),
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure ->
                when (result.value) {
                    is UserError.UsernameCannotBeBlank -> Problem.UsernameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.UsernameToLong -> Problem.UsernameToLong.response(HttpStatus.BAD_REQUEST)
                    is UserError.UsernameAlreadyExists -> Problem.UsernameAlreadyInUse.response(HttpStatus.CONFLICT)
                    is UserError.SessionExpired -> Problem.SessionExpired.response(HttpStatus.UNAUTHORIZED)
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.value)
                }
        }
    }

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result: Either<UserError, User> = userService.getUserById(id)
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure ->
                when (result.value) {
                    is UserError.SessionExpired -> Problem.SessionExpired.response(HttpStatus.UNAUTHORIZED)
                    is UserError.NegativeIdentifier -> Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST)
                    is UserError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.value)
                }
        }
    }

    @GetMapping("/search/{username}")
    fun searchUser(
        @PathVariable username: String,
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @RequestParam(required = false, defaultValue = "0") skip: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result: Either<UserError, List<User>> =
            userService.findUserByUsername(username, limit, skip)
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure ->
                when (result.value) {
                    is UserError.SessionExpired -> Problem.SessionExpired.response(HttpStatus.UNAUTHORIZED)
                    is UserError.UsernameCannotBeBlank -> Problem.UsernameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
                    is UserError.NegativeLimit -> Problem.NegativeLimit.response(HttpStatus.BAD_REQUEST)
                    is UserError.NegativeSkip -> Problem.NegativeSkip.response(HttpStatus.BAD_REQUEST)
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.value)
                }
        }
    }

    @PostMapping("/delete")
    fun deleteUser(
        user: AuthenticatedUser,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<*> {
        return when (val result: Either<UserError, Unit> = userService.deleteUser(user.user.id)) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(null)
            is Failure ->
                when (result.value) {
                    is UserError.SessionExpired -> Problem.SessionExpired.response(HttpStatus.UNAUTHORIZED)
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.value)
                }
        }
    }
}
