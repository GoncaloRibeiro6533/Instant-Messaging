package controllers

import ChannelService
import Failure
import Role
import Success
import models.channel.CreateChannelInputModel
import models.channel.ChannelOutputModel
import models.user.UserIdentifiers
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
                when (result.value) {
                    is ChannelError.ChannelNotFound -> ResponseEntity.notFound().build()
                    is ChannelError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }

    @GetMapping("/{name}")
    fun getChannelByName(
        @PathVariable name: String,
        @RequestHeader("Authorization") token: String,
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @RequestParam(required = false, defaultValue = "0") skip: Int,
        ): ResponseEntity<Any> {
        return when (val result = channelService.getChannelByName(token, name, limit, skip)) {
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
                when (result.value) {
                    is ChannelError.ChannelNotFound -> ResponseEntity.notFound().build()
                    is ChannelError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }

    @PostMapping
    fun createChannel(
        @RequestBody request: CreateChannelInputModel,
    ): ResponseEntity<Any> {
        return when (
            val result =
                channelService.createChannel(
                    name = request.name,
                    creatorId = request.creatorId,
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
                when (result.value) {
                    is ChannelError.InvalidChannelName -> ResponseEntity.badRequest().body(result.value)
                    is ChannelError.UserNotFound -> ResponseEntity.badRequest().body(result.value)
                    is ChannelError.InvalidVisibility -> ResponseEntity.badRequest().body(result.value)
                    is ChannelError.ChannelNameAlreadyExists -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
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
                when (result.value) {
                    is ChannelError.ChannelNotFound -> ResponseEntity.notFound().build()
                    is ChannelError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
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
                when (result.value) {
                    is ChannelError.UserNotFound -> ResponseEntity.notFound().build()
                    is ChannelError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
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
                when (result.value) {
                    is ChannelError.UserNotFound -> ResponseEntity.notFound().build()
                    is ChannelError.ChannelNotFound -> ResponseEntity.notFound().build()
                    is ChannelError.UserAlreadyInChannel -> ResponseEntity.badRequest().body(result.value)
                    is ChannelError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
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
                when (result.value) {
                    is ChannelError.ChannelNotFound -> ResponseEntity.notFound().build()
                    is ChannelError.InvalidChannelName -> ResponseEntity.badRequest().body(result.value)
                    is ChannelError.ChannelNameAlreadyExists -> ResponseEntity.badRequest().body(result.value)
                    is ChannelError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
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
                when (result.value) {
                    is ChannelError.UserNotFound -> ResponseEntity.notFound().build()
                    is ChannelError.ChannelNotFound -> ResponseEntity.notFound().build()
                    is ChannelError.NegativeIdentifier -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }
}
