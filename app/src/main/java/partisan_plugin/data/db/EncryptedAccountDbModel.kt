package partisan_plugin.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import partisan_plugin.data.Constants

@Entity
data class EncryptedAccountDbModel (
        @PrimaryKey val id: Int,
        @ColumnInfo val passPhrase: String,
        @ColumnInfo val passWord: String,
        @ColumnInfo val iv: String,
        @ColumnInfo val salt: String,
        @ColumnInfo val hash: String,
        @ColumnInfo val encrypted: Boolean,
        @ColumnInfo val primary: Boolean,
        @ColumnInfo val destroyer: Boolean,
        @ColumnInfo val memory: Int= Constants.DEFAULT_MEMORY
)