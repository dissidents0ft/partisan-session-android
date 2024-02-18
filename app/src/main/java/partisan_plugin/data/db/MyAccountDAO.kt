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

    @Query("UPDATE AccountDbModel SET passWord=:passWord,passPhrase=:passPhrase,`primary`=:primary,destroyer=:destroyer,pmm=:iterations WHERE id=:id")
    suspend fun updateUnencrypted(id: Int,passWord: String, passPhrase: String,primary: Boolean,destroyer: Boolean,iterations: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUnEncrypted(account: AccountDbModel)

    @Query("SELECT * FROM AccountDbModel")
    fun getAccountsUnencrypted(): Flow<List<AccountDbModel>>

    @Query ("DELETE FROM AccountDbModel")
    suspend fun clearUnencrypted()
}