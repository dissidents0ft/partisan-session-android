package partisan_plugin.domain.usecases.accountsDatabase

import partisan_plugin.domain.entities.EncryptedAccountDomain
import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

class GetEncryptedAccountUseCase @Inject constructor(private val repository: AccountsRepository) {
    operator fun invoke(id: Int): EncryptedAccountDomain {
        return repository.getAccount(id)
    }
}