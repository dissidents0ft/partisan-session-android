package partisan_plugin.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AccountDbModel::class, EncryptedAccountDbModel::class], version = 1)
abstract class AccountsDatabase: RoomDatabase() {
    abstract fun myAccountDao(): MyAccountDAO

    companion object {

        private const val DB_NAME = "accounts_db"
        fun create(context: Context): AccountsDatabase {

            return Room.databaseBuilder(
                    context,
                    AccountsDatabase::class.java,
                    DB_NAME
            ).build()
        }
    }
}