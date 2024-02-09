package sapala.s2sauthservice

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import sapala.s2sauthservice.config.Env

@SpringBootApplication
class S2sAuthServiceApplication(env: Env) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(S2sAuthServiceApplication::class.java)
    }

    init {
        log.info("Swagger UI available at: http://localhost:{}/swagger-ui/index.html", env.port())
    }
}

fun main(args: Array<String>) {
    runApplication<S2sAuthServiceApplication>(*args)
}
