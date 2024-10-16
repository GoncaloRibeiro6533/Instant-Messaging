package pt.isel.talkRooms

import TransactionManagerJdbi
import configureWithAppRequirements
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.context.annotation.Profile
import kotlinx.datetime.Clock

@SpringBootApplication
class TalkRoomsApplication {
    @Bean
    fun jdbi() =
        Jdbi
            .create(PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            }
            ).configureWithAppRequirements()

    @Bean
    @Profile("jdbi")
    fun trxManagerJdbi(jdbi: Jdbi): TransactionManagerJdbi = TransactionManagerJdbi(jdbi)

    @Bean
    fun clock() = Clock.System
}

fun main() {
    runApplication<TalkRoomsApplication>()
}
