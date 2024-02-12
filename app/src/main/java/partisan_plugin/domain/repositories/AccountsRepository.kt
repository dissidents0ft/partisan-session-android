package partisan_plugin.domain.repositories

import kotlinx.coroutines.flow.Flow
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.entities.EncryptedAccountDomain

interface AccountsRepository {

    suspend fun addUnencryptedAccount(name: String, passPhrase: String, passWord: String, primary: Boolean,destroyer: Boolean, iterations: Int)

    suspend fun deleteAccount(id: Int)
    fun getAccount(id: Int): EncryptedAccountDomain
    fun getPrimaryAccount(): EncryptedAccountDomain
    suspend fun encryptDatabase()
    suspend fun decryptItem(pass: String, iterations: Int): Int?
    suspend fun encryptItem()
    fun getDecryptedAccount(): EncryptedAccountDomain
    val accountsUnencrypted: Flow<List<AccountDataDomain>>
    suspend fun updateUnencrypted(account: AccountDataDomain)
}