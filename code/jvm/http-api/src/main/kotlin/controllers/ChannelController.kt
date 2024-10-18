package controllers

import AuthenticatedUser
import ChannelError
import ChannelService
import Failure
import Role
import Success
import models.channel.ChannelOutputModel
import models.channel.CreateChannelInputModel
import models.user.UserIdentifiers
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/channels")
class ChannelController(
    private val channelService: ChannelService,
) {
    @GetMapping("/{id}")
    fun getChannelById(
        @PathVariable id: Int,
    ): ResponseEntity<Any> {
        return when (val result = channelService.getChannelById(id)) {
            is Success -> {
                val outputModel =
                    ChannelOutputModel(
                        id = result.value.id,
                        name = result.value.name,
                        creator = result.value.creator.username,
                        visibility = result.value.visibility,
                    )
                ResponseEntity.ok(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @GetMapping("/{name}")
    fun getChannelByName(
        @PathVariable name: String,
        user: AuthenticatedUser,
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @RequestParam(required = false, defaultValue = "0") skip: Int,
    ): ResponseEntity<Any> {
        return when (val result = channelService.getChannelByName(user.user.id, name, limit, skip)) {
            is Success -> {
                val outputModel =
                    result.value.map {
                        ChannelOutputModel(
                            id = it.id,
                            name = it.name,
                            creator = it.creator.username,
                            visibility = it.visibility,
                        )
                    }
                ResponseEntity.ok(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @PostMapping
    fun createChannel(
        @RequestBody request: CreateChannelInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
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
                        creator = result.value.creator.username,
                        visibility = result.value.visibility,
                    )
                ResponseEntity
                    .status(201)
                    .body(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @GetMapping("/{channelId}/members")
    fun getChannelMembers(
        @PathVariable channelId: Int,
    ): ResponseEntity<Any> {
        return when (val result = channelService.getChannelMembers(channelId)) {
            is Success -> {
                val outputModel =
                    result.value.map {
                        UserIdentifiers(
                            id = it.id,
                            username = it.username,
                        )
                    }
                ResponseEntity.ok(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @GetMapping("/user/{userId}")
    fun getChannelsOfUser(
        @PathVariable userId: Int,
    ): ResponseEntity<Any> {
        return when (val result = channelService.getChannelsOfUser(userId)) {
            is Success -> {
                val outputModel =
                    result.value.map {
                        ChannelOutputModel(
                            id = it.id,
                            name = it.name,
                            creator = it.creator.username,
                            visibility = it.visibility,
                        )
                    }
                ResponseEntity.ok(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @PutMapping("/{channelId}/members/{userId}")
    fun addUserToChannel(
        @PathVariable channelId: Int,
        @PathVariable userId: Int,
        @RequestParam role: Role,
    ): ResponseEntity<Any> {
        return when (val result = channelService.addUserToChannel(userId, channelId, role)) {
            is Success -> {
                val outputModel =
                    ChannelOutputModel(
                        id = result.value.id,
                        name = result.value.name,
                        creator = result.value.creator.username,
                        visibility = result.value.visibility,
                    )
                ResponseEntity.ok(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @PutMapping("/{channelId}")
    fun updateChannelName(
        @PathVariable channelId: Int,
        @RequestParam name: String,
    ): ResponseEntity<Any> {
        return when (val result = channelService.updateChannelName(channelId, name)) {
            is Success -> {
                val outputModel =
                    ChannelOutputModel(
                        id = result.value.id,
                        name = result.value.name,
                        creator = result.value.creator.username,
                        visibility = result.value.visibility,
                    )
                ResponseEntity.ok(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    @PutMapping("/{channelId}/leave/{userId}")
    fun leaveChannel(
        @PathVariable channelId: Int,
        @PathVariable userId: Int,
    ): ResponseEntity<Any> {
        return when (val result = channelService.leaveChannel(userId, channelId)) {
            is Success -> {
                val outputModel =
                    ChannelOutputModel(
                        id = result.value.id,
                        name = result.value.name,
                        creator = result.value.creator.username,
                        visibility = result.value.visibility,
                    )
                ResponseEntity.ok(outputModel)
            }
            is Failure ->
                handleChannelError(result.value)
        }
    }

    fun handleChannelError(error: ChannelError): ResponseEntity<Any> {
        return when (error) {
            is ChannelError.ChannelNotFound -> ResponseEntity.notFound().build()
            is ChannelError.InvalidChannelName -> ResponseEntity.unprocessableEntity().body(error)
            is ChannelError.InvalidVisibility -> ResponseEntity.unprocessableEntity().body(error)
            is ChannelError.NegativeIdentifier -> ResponseEntity.badRequest().body(error)
            is ChannelError.UserNotFound -> ResponseEntity.unprocessableEntity().body(error)
            is ChannelError.UserAlreadyInChannel -> ResponseEntity.unprocessableEntity().body(error)
            is ChannelError.ChannelNameAlreadyExists -> ResponseEntity.unprocessableEntity().body(error)
            is ChannelError.InvalidSkip -> ResponseEntity.unprocessableEntity().body(error)
            is ChannelError.InvalidLimit -> ResponseEntity.unprocessableEntity().body(error)
            else -> ResponseEntity.internalServerError().body("Internal server error")
        }
    }
}
