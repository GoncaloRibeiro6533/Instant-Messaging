package pt.isel.talkRooms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TalkRoomsApplication

fun main(args: Array<String>) {
	runApplication<TalkRoomsApplication>(*args)
}
