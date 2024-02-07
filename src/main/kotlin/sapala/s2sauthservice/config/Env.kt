package sapala.s2sauthservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class Env {
    @Value("\${build.version}")
    private val version: String? = null
    fun version() = version!!

    @Value("\${token.expiration.time}")
    private val tokenExpirationTime: Duration? = null
    fun tokenExpirationTime() = tokenExpirationTime!!
}
