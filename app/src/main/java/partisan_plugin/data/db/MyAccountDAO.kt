package partisan_plugin.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface MyAccountDAO {
    @Query("DELETE FROM AccountDbModel WHERE id=:id")
    suspend fun deleteUnencrypted(id:Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEncrypted(account: EncryptedAccountDbModel)

    @Query("UPDATE AccountDbModel SET name=:name,passWord=:passWord,passPhrase=:passPhrase,`primary`=:primary,destroyer=:destroyer,iterations=:iterations WHERE id=:id")
    suspend fun updateUnencrypted(id: Int,name: String,passWord: String, passPhrase: String,primary: Boolean,destroyer: Boolean,iterations: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUnEncrypted(account: AccountDbModel)

    @Query("SELECT * FROM EncryptedAccountDbModel WHERE id=:id")
    fun getAccountEncrypted(id: Int): List<EncryptedAccountDbModel>

    @Query("SELECT * FROM AccountDbModel")
    fun getAccountsUnencrypted(): Flow<List<AccountDbModel>>

    @Query("SELECT * FROM EncryptedAccountDbModel")
    fun getAccountsEncrypted(): Flow<List<EncryptedAccountDbModel>>

    @Query("SELECT * FROM EncryptedAccountDbModel WHERE `primary`=1")
    fun getPrimaryAccount(): List<EncryptedAccountDbModel>

    @Query("SELECT * From EncryptedAccountDbModel WHERE encrypted=0 AND `primary`=0")
    fun getDecryptedAccount(): List<EncryptedAccountDbModel>

    @Query ("DELETE FROM AccountDbModel")
    suspend fun clearUnencrypted()
}