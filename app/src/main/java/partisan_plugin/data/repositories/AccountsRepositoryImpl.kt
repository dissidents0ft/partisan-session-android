package partisan_plugin.data.repositories

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import partisan_plugin.data.Constants
import partisan_plugin.data.crypto.AesCbcUpdated
import partisan_plugin.data.dataGenerators.GenerateRandomData
import partisan_plugin.data.db.AccountDbModel
import partisan_plugin.data.db.EncryptedAccountDbModel
import partisan_plugin.data.db.MyAccountDAO
import partisan_plugin.data.mappers.AccountMapper
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.entities.CipherTextIvMac
import partisan_plugin.domain.entities.EncryptedAccountDomain
import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

/**
 * Repository for tables with accounts
 */
class AccountsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context,
                             private val myAccountDao: MyAccountDAO,
                             private val mapper: AccountMapper): AccountsRepository {
    /**
     * Flow of unencrypted accounts
     */
    override val accountsUnencrypted = myAccountDao.getAccountsUnencrypted().map { it.map { account -> mapper.mapDbToDt(account) } }

    /**
     * Function for adding unencrypted account in table
     */
    override suspend fun addUnencryptedAccount(passPhrase: String, passWord: String, primary: Boolean,destroyer: Boolean, iterations: Int) {
        val account = AccountDbModel(passPhrase = passPhrase, passWord = passWord, primary = primary, destroyer = destroyer, pmm = iterations)
        myAccountDao.upsertUnEncrypted(account)
    }

    /**
     * Function for editing unencrypted account in table
     */
    override suspend fun updateUnencrypted(account: AccountDataDomain) {
        myAccountDao.updateUnencrypted(account.id,account.passWord,account.passPhrase,account.primary,account.destroyer,account.memory)
    }
    /**
     * Function for getting number of accounts, added by user
     */
    override suspend fun getSize(): Int = myAccountDao.getAccountsUnencrypted().first().size


    /**
     * Function for deleting unencrypted account
     * @param id - id of account to delete
     */
    override suspend fun deleteAccount(id: Int) {
        myAccountDao.deleteUnencrypted(id)
    }

    /**
     * Function for encrypting account and inserting in new table
     * @param account - unencrypted account
     * @param id - account id in new table
     */
    private suspend fun insertEncryptedAccount(account: AccountDbModel, id: Int) {
        myAccountDao.upsertEncrypted(encryptAccount(account,id))
    }

    /**
     * Function for generating fake Session seed, encrypting in and inserting in table
     * @param id - account id in new table
     */
    private suspend fun insertMockAccount(id: Int) {
        val keys = AesCbcUpdated.generateKey()
        val salt = AesCbcUpdated.generateSalt()
        val seed = GenerateRandomData.generateRandomSeed(context) //generating random seed using built-in Session functions
        val passPhraseEncrypted = AesCbcUpdated.encrypt(seed,keys)
        val accountEncrypted = EncryptedAccountDbModel(id = id,passPhrase = passPhraseEncrypted.getCypherString(), passWord = "", iv = passPhraseEncrypted.getIvString(), salt = Base64.encodeToString(salt, AesCbcUpdated.BASE64_FLAGS),hash=passPhraseEncrypted.getMacString(),encrypted = true, primary = false, destroyer = false)
        myAccountDao.upsertEncrypted(accountEncrypted)
    }

    /**
     * Function for inserting primary account in table. Primary account should not be encrypted.
     * @param account - account to insert
     * @param id - account id in new table
     */
    private suspend fun insertPrimaryAccount(account: AccountDbModel, id: Int) {
        myAccountDao.upsertEncrypted(EncryptedAccountDbModel(id=id, passWord = "", passPhrase = account.passPhrase, iv = "", salt = "", hash = "", encrypted = false, primary = account.primary, destroyer = false, memory = account.pmm))
    }

    /**
     * Function for getting encrypted account by id
     * @param id - account id
     * @return encrypted account with specified id
     */
    override fun getAccount(id: Int): EncryptedAccountDomain {
        return mapper.mapEncryptedDbToDt(myAccountDao.getAccountEncrypted(id)[0])
    }

    /**
     * Function for getting primary account
     * @return primary account
     */
    override fun getPrimaryAccount(): EncryptedAccountDomain {
        return mapper.mapEncryptedDbToDt(myAccountDao.getPrimaryAccount()[0])
    }

    /**
     * Function for encrypting database. Copy data from table with unencrypted accounts, clears table with unencrypted accounts, insert encrypted accounts and fake encrypted data in new table.
     */
    override suspend fun encryptDatabase() {
        val data = myAccountDao.getAccountsUnencrypted().first() //copying data from table with unencrypted accounts
        myAccountDao.clearUnencrypted() //clearing table with unencrypted accounts
        val size = data.size
        val selectedNumbers = GenerateRandomData.generateRandomDistinctNumbers(size) //generating random positions in new table for all real accounts.
        var i = 0
        coroutineScope {
            val jobs = mutableListOf<Job>()
            for (accountIndex in 0 until Constants.DEFAULT_DATABASE_SIZE) {
                val job = if (accountIndex in selectedNumbers) {
                    val account = data[i]
                    i++
                    if (account.primary) {
                        coroutineScope { launch { insertPrimaryAccount(account, accountIndex) } } //inserting primary accounts
                    } else {
                        coroutineScope { launch { insertEncryptedAccount(account, accountIndex) } } //inserting encrypted secret accounts
                    }
                } else {
                    coroutineScope { launch { insertMockAccount(accountIndex) } } //inserting encrypted fake accounts
                }
                jobs.add(job)
            }
            jobs.joinAll() //executing all operations in parallel
        }
    }

    /**
     * Function for encrypting account.
     * @param account - unencrypted account
     * @param id - id of account in new table
     * @return encrypted account
     */
    private fun encryptAccount(account: AccountDbModel, id: Int): EncryptedAccountDbModel {
        val salt = AesCbcUpdated.generateSalt()
        val keys = AesCbcUpdated.generateKeyFromPassword(account.passWord,salt)
        val passPhraseEncrypted = AesCbcUpdated.encrypt(account.passPhrase,keys)
        return EncryptedAccountDbModel(id=id,passPhrase = passPhraseEncrypted.getCypherString(), passWord = "", iv= passPhraseEncrypted.getIvString(), hash = passPhraseEncrypted.getMacString(), salt = Base64.encodeToString(salt,AesCbcUpdated.BASE64_FLAGS), encrypted = true, primary = account.primary, destroyer = account.destroyer)
    }

    /**
     * Function for encrypting secret account, temporarily decrypted by user.
     * @param account - unencrypted account
     * @return encrypted account
     */
    private fun encryptDecryptedAccount(account: EncryptedAccountDbModel): EncryptedAccountDbModel {
        val salt = account.salt
        val keys = AesCbcUpdated.generateKeyFromPassword(account.passWord, Base64.decode(salt, AesCbcUpdated.BASE64_FLAGS))
        val passPhraseEncrypted = AesCbcUpdated.encrypt(account.passPhrase, keys)
        return EncryptedAccountDbModel(id = account.id, passPhrase = passPhraseEncrypted.getCypherString(), passWord = "", iv = passPhraseEncrypted.getIvString(), hash = passPhraseEncrypted.getMacString(), salt = account.salt, encrypted = true, primary = account.primary, destroyer = account.destroyer)
    }

    /**
     * Function that attempts to decrypt account from table of encrypted accounts using password given by user.
     * @param account - encrypted account
     * @param passWord - password given by user
     * @return decrypted account or null if decryption was unsuccessful
     */
    private fun decryptAccount(account: EncryptedAccountDbModel, passWord: String, memory: Int): EncryptedAccountDbModel? {
        val civ = CipherTextIvMac(account.passPhrase, account.iv, account.hash)
        val secretKeys = AesCbcUpdated.generateKeyFromPassword(passWord, Base64.decode(account.salt, AesCbcUpdated.BASE64_FLAGS))
        val passPhraseDecrypted = AesCbcUpdated.decryptString(civ, secretKeys) ?: return null
        return EncryptedAccountDbModel(id = account.id, passPhrase = passPhraseDecrypted, passWord = passWord, iv = account.iv, hash = account.hash, salt = account.salt, encrypted = false, primary = account.primary, destroyer = account.destroyer, memory = memory)
    }

    /**
     * Function that iterates over all items in table of encrypted accounts and tries to decrypt some account using password given by user.
     * @param pass - password given by user
     * @return id of decrypted account or null if account corresponding to this password was not found
     */
    override suspend fun decryptItem(pass: String, memory: Int): Int? = coroutineScope{
        val result = myAccountDao.getAccountsEncrypted().first()
                .filterNot { it.primary }
                .map {
            async { decryptAccount(it, pass, memory) }
        }.awaitAll().find { it!=null }
        if (result!=null) {
            myAccountDao.upsertEncrypted(result)
            return@coroutineScope result.id
        }
        return@coroutineScope null
    }

    /**
     * Function for getting secret account previously decrypted by user
     * @return decrypted account
     */
    override fun getDecryptedAccount(): EncryptedAccountDomain {
        val account = myAccountDao.getDecryptedAccount()[0]
        return mapper.mapEncryptedDbToDt(account)
    }

    /**
     * Function that finds account decrypted by user, encrypts it and update in table
     * @return decrypted account
     */
    override suspend fun encryptItem() {
        val account = myAccountDao.getDecryptedAccount()[0]
        myAccountDao.upsertEncrypted(encryptDecryptedAccount(account))
    }
}