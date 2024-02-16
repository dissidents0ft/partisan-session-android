package partisan_plugin.domain.usecases.accountsDatabase

import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

class GetNumberOfItemsUseCase @Inject constructor(private val repository: AccountsRepository) {
    suspend operator fun invoke() : Int{
        return repository.getSize()
    }
}