package partisan_plugin.data.repositories
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import partisan_plugin.TopLevelFunctions.clear
import partisan_plugin.data.Constants
import partisan_plugin.data.crypto.PartisanEncryption
import partisan_plugin.data.dataGenerators.GenerateRandomData
import partisan_plugin.data.db.AccountDbModel
import partisan_plugin.data.db.AccountsDatabase
import partisan_plugin.data.db.MyAccountDAO
import partisan_plugin.data.mappers.AccountMapper
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

/**
 * Repository for tables with accounts
 */
class AccountsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context,
                             private val myAccountDao: MyAccountDAO,
                             private val mapper: AccountMapper,
                             private val partisanEncryption: PartisanEncryption): AccountsRepository {
    /**
     * Flow of unencrypted accounts
     */
    override val accountsUnencrypted = myAccountDao.getAccountsUnencrypted().map { it.map { account -> mapper.mapDbToDt(account) } }

    /**
     * Function for adding unencrypted account in table
     */
    override suspend fun addUnencryptedAccount(passPhrase: String, passWord: String, primary: Boolean, destroyer: Boolean, iterations: Int) {
        val account = AccountDbModel(passPhrase = passPhrase, passWord = passWord, primary = primary, destroyer = destroyer, pmm = iterations)
        myAccountDao.upsertUnEncrypted(account)
    }

    /**
     * Function for editing unencrypted account in table
     */
    override suspend fun updateUnencrypted(account: AccountDataDomain) {
        myAccountDao.updateUnencrypted(account.id, account.passWord, account.passPhrase, account.primary, account.destroyer, account.memory)
    }

    /**
     * Function for getting number of accounts, added by user
     */
    override suspend fun getSize(): Int = myAccountDao.getAccountsUnencrypted().first().size


    /**
     * Function for deleting unencrypted account
     * @param id id of account to delete
     */
    override suspend fun deleteAccount(id: Int) {
        myAccountDao.deleteUnencrypted(id)
    }

    /**
     * Function for encrypting account and saving it
     * @param account unencrypted account
     * @param id new account id
     */
    private suspend fun insertEncryptedAccount(account: AccountDbModel, id: Int) {
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
        myAccountDao.clearUnencrypted()
        context.deleteDatabase(AccountsDatabase.DB_NAME)
    }

    /**
     * Function for encrypting data. Copies data from table with unencrypted accounts, clears database with unencrypted accounts, stores seeds of encrypted accounts and fake encrypted seeds.
     */
    override suspend fun encryptDatabase() {
        val data = myAccountDao.getAccountsUnencrypted().first() //copying data from table with unencrypted accounts
        val primarySeed = data.find { it.primary }?.passPhrase!! //finding primary account seed. There must be one primary account or app will crash!
        val dataFiltered = data.filter { !it.primary } //filtering out primary account from data
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