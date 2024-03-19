package partisan_plugin.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import partisan_plugin.data.ListWithIndexes
import partisan_plugin.data.repositories.AccountsRepositoryImpl
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {
    @Binds
    @Singleton
    abstract fun bindAccountsRepository(accountsRepositoryImpl: AccountsRepositoryImpl): AccountsRepository

    companion object {

        @Provides
        @Singleton
        fun getAccountsFlow(): MutableStateFlow<List<AccountDataDomain>> {
            return MutableStateFlow(listOf())
        }

        @Provides
        @Singleton
        fun provideIndexedListOfAccounts(): ListWithIndexes<AccountDataDomain> {
            return ListWithIndexes()
        }
    }
}