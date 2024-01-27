package sapala.s2sauthservice

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL


@Service
class TokenService {
    companion object {
        private val log = LoggerFactory.getLogger(TokenService::class.java)
    }

    fun requestToken(serviceName: String, tokenReceiverUrl: URL) {
        TODO("Implement token generation")
    }
}
