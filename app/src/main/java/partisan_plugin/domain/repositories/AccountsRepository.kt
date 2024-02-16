package partisan_plugin.domain.repositories

import kotlinx.coroutines.flow.Flow
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.entities.EncryptedAccountDomain

interface AccountsRepository {
    val accountsUnencrypted: Flow<List<AccountDataDomain>>
    suspend fun addUnencryptedAccount(passPhrase: String, passWord: String, primary: Boolean,destroyer: Boolean, iterations: Int)
    suspend fun deleteAccount(id: Int)
    fun getAccount(id: Int): EncryptedAccountDomain
    fun getPrimaryAccount(): EncryptedAccountDomain
    suspend fun encryptDatabase()
    suspend fun decryptItem(pass: String, memory: Int): Int?
    suspend fun encryptItem()
    fun getDecryptedAccount(): EncryptedAccountDomain
    suspend fun updateUnencrypted(account: AccountDataDomain)

    suspend fun getSize(): Int
}