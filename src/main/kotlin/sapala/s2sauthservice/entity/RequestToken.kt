package sapala.s2sauthservice.entity

import io.swagger.v3.oas.annotations.media.Schema
import java.net.URL

data class RequestToken(
    @Schema(example = "example-user-service")
    val serviceName: String,
    @Schema(example = "https://localhost:8081/receive-token")
    val tokenReceiverUrl: URL
)
