package sapala.s2sauthservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.File
import java.net.URI
import java.net.URL
import java.time.Duration

@Configuration
class Env {
    @Value("\${build.version}")
    private val version: String? = null
    fun version() = version!!

    @Value("\${server.port}")
    private val port: Int? = null
    fun port() = port!!

    @Value("\${host}")
    private val host: String? = null
    fun host() = host!!

    @Value("\${token.expiration.time}")
    private val tokenExpirationTime: Duration? = null
    fun tokenExpirationTime() = tokenExpirationTime!!

    @Value("\${token.keys.public.file}")
    private val publicKeyFile: File? = null
    fun publicKeyFile() = publicKeyFile!!

    @Value("\${independent-start}")
    private val independentStart: Boolean? = null

    fun independentStart() = independentStart!!

    fun serverUrl(): URL = URI.create("http://${host()}:${port()}").toURL()
}
