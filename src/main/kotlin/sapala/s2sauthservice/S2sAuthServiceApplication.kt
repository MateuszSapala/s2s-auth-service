package sapala.s2sauthservice

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import sapala.s2sauthservice.config.Env
import sapala.s2sauthservice.services.TokenService
import sapala.s2sauthservice.services.TokenSynchronizationService


@SpringBootApplication
class S2sAuthServiceApplication(private val env: Env, private val tokenSynchronizationService: TokenSynchronizationService) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(S2sAuthServiceApplication::class.java)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun afterStartup() {
        tokenSynchronizationService.requestKeySynchronizationOnOtherInstances()
        log.info("Swagger UI available at: https://localhost:{}/swagger-ui/index.html", env.port())
    }
}

fun main(args: Array<String>) {
    runApplication<S2sAuthServiceApplication>(*args)
}
