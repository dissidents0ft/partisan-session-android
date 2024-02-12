package partisan_plugin.domain.usecases.accountsDatabase

import partisan_plugin.domain.repositories.AccountsRepository
import javax.inject.Inject

class AddUnencryptedAccountUseCase @Inject constructor(private val repository: AccountsRepository) {
    suspend operator fun invoke(name: String, passPhrase: String, passWord: String, primary: Boolean, destroyer: Boolean, iterations: Int) {
        return repository.addUnencryptedAccount(name, passPhrase, passWord, primary, destroyer, iterations)
    }
}