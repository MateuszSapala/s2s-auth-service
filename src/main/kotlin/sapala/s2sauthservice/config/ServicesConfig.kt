package sapala.s2sauthservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
class ServicesConfig {
    @Value("#{\${services}}")
    private val services: HashMap<String, List<URL>>? = null

    fun isCorrectReceiverUrl(serviceName: String, tokenReceiverUrl: URL): Boolean {
        if (!services!!.contains(serviceName)) {
            return false
        }
        if (!services[serviceName]!!.any { it.port == tokenReceiverUrl.port && it.host == tokenReceiverUrl.host && it.protocol == tokenReceiverUrl.protocol }) {
            return false
        }
        return true
    }

    fun s2sAuthServiceUrls(): List<URL> {
        return services!!["s2s-auth-service"]!!
    }
}
