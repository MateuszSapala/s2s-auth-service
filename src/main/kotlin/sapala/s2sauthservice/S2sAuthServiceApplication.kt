package sapala.s2sauthservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class S2sAuthServiceApplication

fun main(args: Array<String>) {
	runApplication<S2sAuthServiceApplication>(*args)
}
