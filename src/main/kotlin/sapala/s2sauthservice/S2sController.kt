package sapala.s2sauthservice

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import sapala.s2sauthservice.entity.PublicKey
import sapala.s2sauthservice.entity.RequestToken

@RestController
@RequestMapping("/s2s-auth-service/api/v1")
class S2sController(private val tokenService: TokenService) {
    companion object {
        private val log = LoggerFactory.getLogger(S2sController::class.java)
    }

    @PostMapping("/request-token")
    @Operation(summary = "Requests a JWT that will be sent in separate requests to the URL specified in the body")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ApiResponses(value = [ApiResponse(responseCode = "204", description = "Accepted")])
    fun requestToken(body: RequestToken) {
        log.info("Service '${body.serviceName}' requested to receive token on '${body.tokenReceiverUrl}'")
        tokenService.requestToken(body.serviceName, body.tokenReceiverUrl)
    }

    @GetMapping("/public-keys")
    @Operation(summary = "Get public keys that can be used to verify token")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Ok")])
    fun getPublicKeys(): Map<String, PublicKey> {
        return tokenService.getPublicKeys()
    }
}
