package partisan_plugin.domain.usecases.accountsDatabase

import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

class GetUnencryptedDataUseCase @Inject constructor(private val repository: AccountsRepository) {
    operator fun invoke() = repository.accountsUnencrypted

}