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
import pt.isel.ChannelError
import pt.isel.ChannelService
import pt.isel.Failure
import pt.isel.Role
import pt.isel.Success
import pt.isel.models.Problem
import pt.isel.models.channel.ChannelList
import pt.isel.models.channel.ChannelOutputModel
import pt.isel.models.channel.CreateChannelInputModel
import pt.isel.models.user.UserIdentifiers
import pt.isel.models.user.UserList

@RestController
@RequestMapping("api/channels")
class ChannelController(
    private val channelService: ChannelService,
) {
    @PostMapping
    fun createChannel(
        @RequestBody request: CreateChannelInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return when (
            val result =
                channelService.createChannel(
                    name = request.name,
                    creatorId = user.user.id,
                    visibility = request.visibility,
                )
        ) {
            is Success -> {
                val outputModel =
                    ChannelOutputModel(
                        id = result.value.id,
                        name = result.value.name,
                        creator =
                            UserIdentifiers(
                                id = result.value.creator.id,
                                username = result.value.creator.username,
                            ),
                        visibility = result.value.visibility,
                    )
                ResponseEntity.status(HttpStatus.CREATED).body(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @PutMapping("/{channelId}/add/{role}")
    fun addUserToChannel(
        @PathVariable channelId: Int,
        @PathVariable role: Role,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return when (val result = channelService.addUserToChannel(user.user.id, channelId, role)) {
            is Success -> {
                val outputModel =
                    ChannelOutputModel(
                        id = result.value.id,
                        name = result.value.name,
                        creator =
                            UserIdentifiers(
                                id = result.value.creator.id,
                                username = result.value.creator.username,
                            ),
                        visibility = result.value.visibility,
                    )
                ResponseEntity.status(HttpStatus.OK).body(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @GetMapping("/{id}")
    fun getChannelById(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return when (val result = channelService.getChannelById(id)) {
            is Success -> {
                val outputModel =
                    ChannelOutputModel(
                        id = result.value.id,
                        name = result.value.name,
                        creator =
                            UserIdentifiers(
                                id = result.value.creator.id,
                                username = result.value.creator.username,
                            ),
                        visibility = result.value.visibility,
                    )
                ResponseEntity.status(HttpStatus.OK).body(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @GetMapping("search/{name}")
    fun getChannelByName(
        @PathVariable name: String,
        user: AuthenticatedUser,
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @RequestParam(required = false, defaultValue = "0") skip: Int,
    ): ResponseEntity<*> {
        return when (val result = channelService.getChannelByName(user.user.id, name, limit, skip)) {
            is Success -> {
                val outputModel =
                    result.value.map {
                        ChannelOutputModel(
                            id = it.id,
                            name = it.name,
                            creator =
                                UserIdentifiers(
                                    id = it.creator.id,
                                    username = it.creator.username,
                                ),
                            visibility = it.visibility,
                        )
                    }
                ResponseEntity.status(HttpStatus.OK).body(ChannelList(outputModel.size, outputModel))
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @GetMapping("/{channelId}/members")
    fun getChannelMembers(
        @PathVariable channelId: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return when (val result = channelService.getChannelMembers(channelId)) {
            is Success -> {
                val outputModel =
                    result.value.map {
                        UserIdentifiers(
                            id = it.id,
                            username = it.username,
                        )
                    }
                ResponseEntity.status(HttpStatus.OK).body(UserList(outputModel, result.value.size))
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @GetMapping("/user/{userId}")
    fun getChannelsOfUser(
        @PathVariable userId: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return when (val result = channelService.getChannelsOfUser(userId)) {
            is Success -> {
                val outputModel =
                    result.value.map {
                        ChannelOutputModel(
                            id = it.id,
                            name = it.name,
                            creator =
                                UserIdentifiers(
                                    id = it.creator.id,
                                    username = it.creator.username,
                                ),
                            visibility = it.visibility,
                        )
                    }
                ResponseEntity.status(HttpStatus.OK).body(ChannelList(outputModel.size, outputModel))
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @PutMapping("/{channelId}/{name}")
    fun updateChannelName(
        @PathVariable channelId: Int,
        @PathVariable name: String,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return when (val result = channelService.updateChannelName(channelId, name, user.user.id)) {
            is Success -> {
                val outputModel =
                    ChannelOutputModel(
                        id = result.value.id,
                        name = result.value.name,
                        creator =
                            UserIdentifiers(
                                id = result.value.creator.id,
                                username = result.value.creator.username,
                            ),
                        visibility = result.value.visibility,
                    )
                ResponseEntity.status(HttpStatus.OK).body(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @PutMapping("/{channelId}/leave/{userId}")
    fun leaveChannel(
        @PathVariable channelId: Int,
        @PathVariable userId: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return when (val result = channelService.leaveChannel(userId, channelId)) {
            is Success -> {
                val outputModel =
                    ChannelOutputModel(
                        id = result.value.id,
                        name = result.value.name,
                        creator =
                            UserIdentifiers(
                                id = result.value.creator.id,
                                username = result.value.creator.username,
                            ),
                        visibility = result.value.visibility,
                    )
                ResponseEntity.status(HttpStatus.OK).body(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    fun handleChannelError(error: ChannelError): ResponseEntity<*> {
        return when (error) {
            is ChannelError.ChannelNotFound -> Problem.ChannelNotFound.response(HttpStatus.NOT_FOUND)
            is ChannelError.ChannelNameAlreadyExists -> Problem.ChannelNameAlreadyExists.response(HttpStatus.CONFLICT)
            is ChannelError.InvalidChannelName -> Problem.InvalidChannelName.response(HttpStatus.BAD_REQUEST)
            is ChannelError.NegativeIdentifier -> Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST)
            is ChannelError.UserAlreadyInChannel -> Problem.UserAlreadyInChannel.response(HttpStatus.BAD_REQUEST)
            is ChannelError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
            is ChannelError.InvalidSkip -> Problem.NegativeSkip.response(HttpStatus.BAD_REQUEST)
            is ChannelError.InvalidLimit -> Problem.NegativeLimit.response(HttpStatus.BAD_REQUEST)
            is ChannelError.InvalidVisibility -> Problem.InvalidVisibility.response(HttpStatus.BAD_REQUEST)
            is ChannelError.Unauthorized -> Problem.Unauthorized.response(HttpStatus.UNAUTHORIZED)
            is ChannelError.UserNotInChannel -> Problem.UserNotInChannel.response(HttpStatus.BAD_REQUEST)
        }
    }
}
