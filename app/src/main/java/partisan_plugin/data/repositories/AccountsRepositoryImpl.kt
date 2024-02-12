package partisan_plugin.data.repositories

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import partisan_plugin.data.Constants
import partisan_plugin.data.crypto.AesCbcUpdated
import partisan_plugin.data.dataGenerators.GenerateRandomData
import partisan_plugin.data.db.AccountDbModel
import partisan_plugin.data.db.EncryptedAccountDbModel
import partisan_plugin.data.db.MyAccountDAO
import partisan_plugin.data.mappers.AccountMapper
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.entities.EncryptedAccountDomain
import partisan_plugin.domain.entities.EncryptionDataContainer
import partisan_plugin.domain.repositories.AccountsRepository
import javax.crypto.SecretKey
import javax.inject.Inject

class AccountsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context,
                             private val myAccountDao: MyAccountDAO,
                             private val mapper: AccountMapper): AccountsRepository {

    override val accountsUnencrypted = myAccountDao.getAccountsUnencrypted().map { it.map { account -> mapper.mapDbToDt(account) } }

    override suspend fun addUnencryptedAccount(name: String, passPhrase: String, passWord: String, primary: Boolean,destroyer: Boolean, iterations: Int) {
        val account = AccountDbModel(name=name, passPhrase = passPhrase, passWord = passWord, primary = primary, destroyer = destroyer, iterations = iterations)
        myAccountDao.upsertUnEncrypted(account)
    }

    override suspend fun updateUnencrypted(account: AccountDataDomain) {
        myAccountDao.updateUnencrypted(account.id,account.name,account.passWord,account.passPhrase,account.primary,account.destroyer,account.iterations)
    }

    override suspend fun deleteAccount(id: Int) {
        myAccountDao.deleteUnencrypted(id)
    }

    private suspend fun insertEncryptedAccount(account: AccountDbModel, id: Int) {
        myAccountDao.upsertEncrypted(encryptAccount(account,id))
    }

    private suspend fun insertMockAccount(id: Int) {
        val key = AesCbcUpdated.generateKey()
        val iv = AesCbcUpdated.generateIv()
        val nameEncrypted = AesCbcUpdated.encrypt(GenerateRandomData.generateRandomName(),iv,key)
        val passPhraseEncrypted = AesCbcUpdated.encrypt(GenerateRandomData.generateRandomSeed(context),iv,key)
        val accountEncrypted = EncryptedAccountDbModel(id = id,name=nameEncrypted, passPhrase = passPhraseEncrypted, passWord = "", iv = Base64.encodeToString(iv,AesCbcUpdated.BASE64_FLAGS),encrypted = true, primary = false, destroyer = false)
        myAccountDao.upsertEncrypted(accountEncrypted)
    }

    private suspend fun insertPrimaryAccount(account: AccountDbModel, id: Int) {
        myAccountDao.upsertEncrypted(EncryptedAccountDbModel(id=id,name = account.name, passWord = "", passPhrase = account.passPhrase, iv = "", encrypted = false, primary = account.primary, destroyer = false, iterations = account.iterations))
    }

    override fun getAccount(id: Int): EncryptedAccountDomain {
        return mapper.mapEncryptedDbToDt(myAccountDao.getAccountEncrypted(id)[0])
    }

    override fun getPrimaryAccount(): EncryptedAccountDomain {
        return mapper.mapEncryptedDbToDt(myAccountDao.getPrimaryAccount()[0])
    }

    override suspend fun encryptDatabase() {
        val data = myAccountDao.getAccountsUnencrypted().first()
        myAccountDao.clearUnencrypted()
        val size = data.size
        val selectedNumbers = GenerateRandomData.generateRandomDistinctNumbers(size)
        var i = 0
        for (accountIndex in 0 until Constants.DEFAULT_DATABASE_SIZE) {
            if (accountIndex in selectedNumbers) {
                val account = data[i]
                i ++
                if (account.primary) {
                    insertPrimaryAccount(account,accountIndex)
                } else {
                    insertEncryptedAccount(account,accountIndex)
                }
            } else {
                insertMockAccount(accountIndex)
            }
        }
    }

    private fun encryptAccount(account: AccountDbModel, id: Int): EncryptedAccountDbModel {
        val iv = AesCbcUpdated.generateIv()
        val key = getSecretKeyFromPass(account.passWord)
        val nameEncrypted = AesCbcUpdated.encrypt(account.name,iv,key)
        val passPhraseEncrypted = AesCbcUpdated.encrypt(account.passPhrase,iv,key)
        return EncryptedAccountDbModel(id=id,name=nameEncrypted, passPhrase = passPhraseEncrypted, passWord = "", iv= Base64.encodeToString(iv,AesCbcUpdated.BASE64_FLAGS), encrypted = true, primary = account.primary, destroyer = account.destroyer, iterations = Constants.DEFAULT_ITERATIONS)
    }

    private fun encryptDecryptedAccount(account: EncryptedAccountDbModel): EncryptedAccountDbModel {
        val iv = AesCbcUpdated.generateIv()
        val key = getSecretKeyFromPass(account.passWord)
        val nameEncrypted = AesCbcUpdated.encrypt(account.name,iv,key)
        val passPhraseEncrypted = AesCbcUpdated.encrypt(account.passPhrase,iv,key)
        return EncryptedAccountDbModel(id=account.id,name=nameEncrypted, passPhrase = passPhraseEncrypted, passWord = "", iv= Base64.encodeToString(iv,AesCbcUpdated.BASE64_FLAGS), encrypted = true, primary = account.primary, destroyer = account.destroyer, iterations = Constants.DEFAULT_ITERATIONS)
    }

    private fun getSecretKeyFromPass(passWord: String): SecretKey {
        val salt = AesCbcUpdated.getSalt(context)
        return AesCbcUpdated.generateKeyFromPassword(passWord,salt)
    }

    private fun decryptAccount(account: EncryptedAccountDbModel, key: SecretKey, passWord: String, iterations: Int): EncryptedAccountDbModel? {
        val nameDecrypted = AesCbcUpdated.decryptString(EncryptionDataContainer(account.iv,account.name),key)
                ?: return null
        val passPhraseDecrypted = AesCbcUpdated.decryptString(EncryptionDataContainer(account.iv,account.passPhrase),key) ?: return null
        return EncryptedAccountDbModel(id=account.id,name=nameDecrypted, passPhrase = passPhraseDecrypted, passWord = passWord,iv=account.iv, encrypted = false, primary = account.primary, destroyer = account.destroyer, iterations = iterations)
    }

    override suspend fun decryptItem(pass: String, iterations: Int): Int? {
        val data = myAccountDao.getAccountsEncrypted().first()
        val key = getSecretKeyFromPass(pass)
        for (account in data) {
            if (account.primary) {
                continue
            }
            val accountDecrypted = decryptAccount(account,key,pass,iterations) ?: continue
            myAccountDao.upsertEncrypted(accountDecrypted)
            return accountDecrypted.id
        }
        return null
    }

    override fun getDecryptedAccount(): EncryptedAccountDomain {
        val account = myAccountDao.getDecryptedAccount()[0]
        return mapper.mapEncryptedDbToDt(account)
    }

    override suspend fun encryptItem() {
        val account = myAccountDao.getDecryptedAccount()[0]
        myAccountDao.upsertEncrypted(encryptDecryptedAccount(account))
    }
}