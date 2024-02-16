package partisan_plugin.domain.entities

data class EncryptedAccountDomain(val id: Int, val passPhrase: String, val passWord: String,
                             val primary: Boolean, val iv: String, val salt: String, val hash: String, val encrypted: Boolean, val destroyer: Boolean, val memory: Int)