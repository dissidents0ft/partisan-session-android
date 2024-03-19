package partisan_plugin.data.repositories
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import partisan_plugin.TopLevelFunctions.clear
import partisan_plugin.data.Constants
import partisan_plugin.data.ListWithIndexes
import partisan_plugin.data.crypto.PartisanEncryption
import partisan_plugin.data.dataGenerators.GenerateRandomData
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

/**
 * Repository for tables with accounts
 */
class AccountsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context,
                                                 private val partisanEncryption: PartisanEncryption,
                                                 private val accountsFlow: MutableStateFlow<List<AccountDataDomain>>,
                                                 private val accountsList: ListWithIndexes<AccountDataDomain>): AccountsRepository {
    /**
     * Flow of unencrypted accounts
     */
    override val accountsUnencrypted = accountsFlow.asStateFlow()

    /**
     * Function for adding unencrypted account in table
     */
    override suspend fun addUnencryptedAccount(passPhrase: String, passWord: String, primary: Boolean, destroyer: Boolean, memory: Int) {
        val account = AccountDataDomain(passPhrase = passPhrase, passWord = passWord, primary = primary, destroyer = destroyer, memory = memory)
        accountsList.add(account)
        accountsFlow.emit(accountsList.toList())
    }

    /**
     * Function for editing unencrypted account in table
     */
    override suspend fun updateUnencrypted(account: AccountDataDomain) {
        val oldAccountIndex = accountsList.indexOfFirst { it.index == account.index }
        accountsList[oldAccountIndex] = account
        accountsFlow.emit(accountsList.toList())
    }

    /**
     * Function for getting number of accounts, added by user
     */
    override suspend fun getSize(): Int = accountsList.size


    /**
     * Function for deleting unencrypted account
     * @param id id of account to delete
     */
    override suspend fun deleteAccount(id: Int) {
        val oldAccountIndex = accountsList.indexOfFirst { it.index == id}
        accountsList.removeAt(oldAccountIndex)
        accountsFlow.emit(accountsList.toList())
    }

    /**
     * Function for encrypting account and saving it
     * @param account unencrypted account
     * @param id new account id
     */
    private suspend fun insertEncryptedAccount(account: AccountDataDomain, id: Int) {
        partisanEncryption.encryptRealData(id, account.passWord.toCharArray(), account.passPhrase)
    }

    /**
     * Function for generating fake Session seed, encrypting it and saving
     * @param id new account id
     */
    private suspend fun insertMockAccount(id: Int) {
        partisanEncryption.encryptMockData(id, GenerateRandomData.generateRandomSeed(context))
    }

    /**
     * Function for database clearing after encryption
     */
    private suspend fun clearDatabase() {
        accountsList.clear()
        accountsFlow.emit(accountsList.toList())
    }

    /**
     * Function for encrypting data. Copies data from table with unencrypted accounts, clears database with unencrypted accounts, stores seeds of encrypted accounts and fake encrypted seeds.
     */
    override suspend fun encryptDatabase() {
        val primarySeed = accountsList.find { it.primary }?.passPhrase!! //finding primary account seed. There must be one primary account or app will crash!
        val dataFiltered = accountsList.filter { !it.primary } //filtering out primary account from data
        val size = dataFiltered.size
        val selectedNumbers = GenerateRandomData.generateRandomDistinctNumbers(size) //generating random positions in new table for all real accounts.
        var i = 0
        coroutineScope {
            val clearDatabase = launch { clearDatabase() } //clearing database with unencrypted accounts
            val addPrimarySeed = launch { partisanEncryption.setPrimarySeed(primarySeed) }  //storing primary seed
            val jobs = mutableListOf(clearDatabase, addPrimarySeed)
            for (accountIndex in 0 until Constants.DEFAULT_DATABASE_SIZE) {
                val job = if (accountIndex in selectedNumbers) {
                    val account = dataFiltered[i]
                    i++
                    launch { insertEncryptedAccount(account, accountIndex) } //inserting encrypted secret accounts
                } else {
                    launch { insertMockAccount(accountIndex) } //inserting encrypted fake accounts
                }
                jobs.add(job)
            }
            jobs.joinAll() //executing all operations in parallel
        }
    }

    /**
     * Function that attempts to decrypt account with given id using password given by user.
     * @param id id of account to decrypt
     * @param passWord password given by user
     * @return id of decrypted account or null if decryption was unsuccessful
     */
    private fun decryptAccount(id: Int, passWord: CharArray, memory: Int): Boolean {
        val passPhrase = partisanEncryption.decryptData(id, passWord)
        if (passPhrase != null) {
            partisanEncryption.setEncryptedSeed(passPhrase)
            throw CancellationException()
        }
        return false
    }

    /**
     * Function that iterates over all encrypted accounts and tries to decrypt some account using password given by user.
     * @param pass password given by user
     * @return id of decrypted account or null if account corresponding to this password was not found
     */
    override suspend fun decryptItem(pass: CharArray, memory: Int): Boolean = coroutineScope {
        return@coroutineScope try {
            (0 until Constants.DEFAULT_DATABASE_SIZE)
                    .map {
                        async { decryptAccount(it, pass, memory) }
                    }.awaitAll()
            false
        } catch (e: CancellationException) {
            pass.clear() //clearing unused password
            true
        }
    }

}