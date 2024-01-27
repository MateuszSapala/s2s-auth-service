package sapala.s2sauthservice

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class S2sAuthServiceApplication{
	companion object {
		val log: Logger = LoggerFactory.getLogger(S2sAuthServiceApplication::class.java)
	}
}

fun main(args: Array<String>) {
	runApplication<S2sAuthServiceApplication>(*args)
	S2sAuthServiceApplication.log.info("Swagger UI available at: http://localhost:8080/swagger-ui/index.html")
}
