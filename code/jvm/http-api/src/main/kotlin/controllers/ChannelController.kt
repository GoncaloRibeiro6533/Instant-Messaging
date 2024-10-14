package controllers

import ChannelService
import Failure
import Success
import models.ChannelInputModel
import models.ChannelOutputModel
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
    ): ResponseEntity<Any> {
        return when (val result = channelService.getChannelByName(name)) {
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
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }

    @PostMapping
    fun createChannel(
        @RequestBody request: ChannelInputModel,
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
                    is ChannelError.ChannelAlreadyExists -> ResponseEntity.badRequest().body(result.value)
                    else -> ResponseEntity.badRequest().body(result.value)
                }
            else -> {
                ResponseEntity.internalServerError().body("Internal server error")
            }
        }
    }
}
