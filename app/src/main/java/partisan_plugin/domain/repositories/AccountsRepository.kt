package partisan_plugin.domain.repositories

import kotlinx.coroutines.flow.Flow
import partisan_plugin.domain.entities.AccountDataDomain

interface AccountsRepository {
    val accountsUnencrypted: Flow<List<AccountDataDomain>>
    suspend fun addUnencryptedAccount(passPhrase: String, passWord: String, primary: Boolean,destroyer: Boolean, iterations: Int)
    suspend fun deleteAccount(id: Int)
    suspend fun encryptDatabase()
    suspend fun decryptItem(pass: CharArray, memory: Int): Boolean
    suspend fun updateUnencrypted(account: AccountDataDomain)

    suspend fun getSize(): Int
}