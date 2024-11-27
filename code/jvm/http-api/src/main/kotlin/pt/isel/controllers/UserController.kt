package pt.isel.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.AuthenticatedUser
import pt.isel.Either
import pt.isel.Failure
import pt.isel.Success
import pt.isel.User
import pt.isel.UserError
import pt.isel.UserService
import pt.isel.models.Problem
import pt.isel.models.user.UserIdentifiers
import pt.isel.models.user.UserList
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput
import pt.isel.models.user.UsernameUpdateInput

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
                handleUserError(result.value)
        }
    }

    @PostMapping("/register/{invitationId}")
    fun register(
        @RequestBody userRegisterInput: UserRegisterInput,
        @PathVariable invitationId: Int,
    ): ResponseEntity<*> {
        val result: Either<UserError, User> =
            userService.createUser(
                userRegisterInput.username.trim(),
                userRegisterInput.email.trim(),
                userRegisterInput.password,
                invitationId,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure ->
                handleUserError(result.value)
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
                handleUserError(result.value)
        }
    }

    // TODO receive a token to logout a specific session, verify in service if the session is from requester
    @PostMapping("/logout")
    fun logout(user: AuthenticatedUser): ResponseEntity<*> {
        return when (val result: Either<UserError, Boolean> = userService.logoutUser(user.token)) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(null)
            is Failure ->
                handleUserError(result.value)
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
                handleUserError(result.value)
        }
    }

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return when (val result: Either<UserError, User> = userService.getUserById(id)) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    UserIdentifiers(result.value.id, result.value.username, result.value.email),
                )
            is Failure ->
                handleUserError(result.value)
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
            is Success -> {
                val userList = result.value.map { UserIdentifiers(it.id, it.username, it.email) }
                ResponseEntity.status(HttpStatus.OK).body(UserList(userList, result.value.size))
            }
            is Failure ->
                handleUserError(result.value)
        }
    }

    @PostMapping("/delete")
    fun deleteUser(user: AuthenticatedUser): ResponseEntity<*> {
        return when (val result: Either<UserError, Unit> = userService.deleteUser(user.user.id)) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(null)
            is Failure ->
                handleUserError(result.value)
        }
    }

    // Only use this endpoint for the PDM project
    @PostMapping("/pdm/register")
    fun registerPDM(
        @RequestBody userRegisterInput: UserRegisterInput,
    ): ResponseEntity<*> {
        val result: Either<UserError, User> =
            userService.registerPDM(
                userRegisterInput.username.trim(),
                userRegisterInput.email.trim(),
                userRegisterInput.password,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure ->
                handleUserError(result.value)
        }
    }

    fun handleUserError(error: UserError): ResponseEntity<*> {
        return when (error) {
            is UserError.SessionExpired -> Problem.SessionExpired.response(HttpStatus.UNAUTHORIZED)
            is UserError.NegativeIdentifier -> Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST)
            is UserError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
            is UserError.UsernameAlreadyExists -> Problem.UsernameAlreadyInUse.response(HttpStatus.CONFLICT)
            is UserError.UsernameCannotBeBlank -> Problem.UsernameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is UserError.PasswordCannotBeBlank -> Problem.PasswordCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is UserError.EmailCannotBeBlank -> Problem.EmailCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is UserError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
            is UserError.EmailAlreadyInUse -> Problem.EmailAlreadyInUse.response(HttpStatus.CONFLICT)
            is UserError.EmailDoesNotMatchInvite -> Problem.EmailDoesNotMatchInvite.response(HttpStatus.BAD_REQUEST)
            is UserError.UsernameToLong -> Problem.UsernameToLong.response(HttpStatus.BAD_REQUEST)
            is UserError.NoMatchingUsername -> Problem.NoMatchingUsername.response(HttpStatus.NOT_FOUND)
            is UserError.NoMatchingPassword -> Problem.NoMatchingPassword.response(HttpStatus.UNAUTHORIZED)
            is UserError.NotFirstUser -> Problem.NotFirstUser.response(HttpStatus.CONFLICT)
            is UserError.WeakPassword -> Problem.WeakPassword.response(HttpStatus.BAD_REQUEST)
            is UserError.NegativeLimit -> Problem.NegativeLimit.response(HttpStatus.BAD_REQUEST)
            is UserError.NegativeSkip -> Problem.NegativeSkip.response(HttpStatus.BAD_REQUEST)
            is UserError.InvitationNotFound -> Problem.InvitationNotFound.response(HttpStatus.BAD_REQUEST)
            is UserError.InvitationAlreadyUsed -> Problem.InvitationAlreadyUsed.response(HttpStatus.CONFLICT)
        }
    }
}
