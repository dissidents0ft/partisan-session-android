package partisan_plugin.domain.entities

data class AccountDataDomain(val id: Int, val passPhrase: String, val passWord: String,
                             val primary: Boolean, val destroyer: Boolean, val memory: Int)