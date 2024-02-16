package partisan_plugin.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import partisan_plugin.data.Constants

@Entity
data class AccountDbModel (
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo val passPhrase: String,
        @ColumnInfo val passWord: String,
        @ColumnInfo val primary: Boolean,
        @ColumnInfo val destroyer: Boolean,
        @ColumnInfo val pmm: Int=Constants.DEFAULT_MEMORY
)