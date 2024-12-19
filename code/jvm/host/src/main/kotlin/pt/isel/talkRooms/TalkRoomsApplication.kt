@file:Suppress("ktlint")

package pt.isel.talkRooms

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.isel.*
import pt.isel.pipeline.AuthenticatedUserArgumentResolver
import pt.isel.pipeline.AuthenticationInterceptor
import java.util.*
import kotlin.time.Duration.Companion.hours

@Configuration
@ComponentScan("pt.isel")
class PipelineConfigurer(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowCredentials(true)
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedOrigins("http://localhost:8080", "http://localhost:8000")
    }
}


@SpringBootApplication
class TalkRoomsApplication {
    @Bean
    fun jdbi() =
        Jdbi
            .create(
                PGSimpleDataSource().apply {
                    setURL(Environment.getDbUrl())
                },
            ).configureWithAppRequirements()

    @Bean
    @Profile("jdbi")
    fun trxManagerJdbi(jdbi: Jdbi): TransactionManagerJdbi = TransactionManagerJdbi(jdbi)

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun clock() = Clock.System

    @Bean
    fun usersDomainConfig() =
        UsersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = 24.hours * 7, // 24.hours,
            tokenRollingTtl = 1.hours,
            maxTokensPerUser = 3,
        )

    @Bean
    fun mailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl().apply {
            host = "smtp.gmail.com"
            port = 587
            username = "talkroomsapplication@gmail.com"
            password = "fhro ynch pdrw hutp"
            javaMailProperties = Properties().apply {
                put("mail.transport.protocol", "smtp")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.debug", "true")
            }
        }
        return mailSender
    }
}

fun main() {
    runApplication<TalkRoomsApplication>()
}
