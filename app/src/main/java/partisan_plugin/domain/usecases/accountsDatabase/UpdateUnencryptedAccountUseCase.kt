package partisan_plugin.domain.usecases.accountsDatabase

import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

class UpdateUnencryptedAccountUseCase @Inject constructor(private val repository: AccountsRepository) {
    suspend operator fun invoke(account: AccountDataDomain) {
        repository.updateUnencrypted(account)
    }
}