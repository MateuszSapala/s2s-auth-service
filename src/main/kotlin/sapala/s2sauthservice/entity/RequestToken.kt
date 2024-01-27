package sapala.s2sauthservice.entity

import io.swagger.v3.oas.annotations.media.Schema
import java.net.URL

data class RequestToken(
    @Schema(example = "some-service")
    val serviceName: String,
    @Schema(example = "https://host:8080/receive-token")
    val tokenReceiverUrl: URL
)
