package partisan_plugin.domain.usecases.accountsDatabase

import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(private val repository: AccountsRepository) {
    suspend operator fun invoke(id: Int) {
        repository.deleteAccount(id)
    }
}