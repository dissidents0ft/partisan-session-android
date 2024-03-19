package partisan_plugin.domain.entities

data class AccountDataDomain(override var index: Int = 0, val passPhrase: String, val passWord: String,
                             val primary: Boolean, val destroyer: Boolean, val memory: Int): Indexed