package pt.isel

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.isel.mapper.InstantMapper
import java.time.Instant

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())
    // registerColumnMapper(SessionMapper()) TODO
    registerColumnMapper(Instant::class.java, InstantMapper())
    return this
}
