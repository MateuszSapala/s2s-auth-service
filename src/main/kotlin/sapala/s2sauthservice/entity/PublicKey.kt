package sapala.s2sauthservice.entity

data class PublicKey(
    val type: String,
    val publicKey: String,
    val generatedBy: String,
    val generatedOn: Long
)
