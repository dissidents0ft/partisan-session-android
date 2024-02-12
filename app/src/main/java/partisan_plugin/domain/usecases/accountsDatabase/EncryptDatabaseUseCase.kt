package partisan_plugin.domain.usecases.accountsDatabase

import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

class EncryptDatabaseUseCase @Inject constructor(private val repository: AccountsRepository) {
    suspend operator fun invoke() {
        repository.encryptDatabase()
    }
}