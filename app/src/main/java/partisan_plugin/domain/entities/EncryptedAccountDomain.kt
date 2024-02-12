package partisan_plugin.domain.entities

data class EncryptedAccountDomain(val id: Int, val name: String, val passPhrase: String, val passWord: String,
                             val primary: Boolean, val iv: String, val encrypted: Boolean, val destroyer: Boolean, val iterations: Int)