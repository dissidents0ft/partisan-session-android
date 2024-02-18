package partisan_plugin.data.mappers

import partisan_plugin.data.db.AccountDbModel
import partisan_plugin.domain.entities.AccountDataDomain
import javax.inject.Inject

class AccountMapper @Inject constructor() {
    fun mapDbToDt(accountDbModel: AccountDbModel) = AccountDataDomain (
            id = accountDbModel.id,
            passPhrase = accountDbModel.passPhrase,
            passWord = accountDbModel.passWord,
            primary = accountDbModel.primary,
            destroyer = accountDbModel.destroyer,
            memory = accountDbModel.pmm
    )


}