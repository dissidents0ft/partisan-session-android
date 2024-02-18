package partisan_plugin.domain.usecases.accountsDatabase

import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

class DecryptItemUseCase @Inject constructor(private val repository: AccountsRepository) {
    suspend operator fun invoke(pass: CharArray, iterations: Int): Boolean {
        return repository.decryptItem(pass,iterations)
    }
}