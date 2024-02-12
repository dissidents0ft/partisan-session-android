package partisan_plugin.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import partisan_plugin.data.repositories.AccountsRepositoryImpl
import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class ActivityRepositoriesModule {
    @Binds
    @Singleton
    abstract fun bindAccountsRepository(accountsRepositoryImpl: AccountsRepositoryImpl): AccountsRepository

    companion object {
        @Provides
        @Singleton
        fun getCoroutineScope(): CoroutineScope {
            return CoroutineScope(Dispatchers.Default)
        }
    }
}