package partisan_plugin.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import partisan_plugin.data.db.AccountsDatabase
import partisan_plugin.data.db.MyAccountDAO
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AccountsDatabaseModule {
    @Provides
    @Singleton
    fun provideDao(accountsDatabase: AccountsDatabase): MyAccountDAO {
        return accountsDatabase.myAccountDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
            @ApplicationContext context: Context,
    ): AccountsDatabase {
        return AccountsDatabase.create(context)
    }
}