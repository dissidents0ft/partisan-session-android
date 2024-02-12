package partisan_plugin.data.mappers

import partisan_plugin.data.db.AccountDbModel
import partisan_plugin.data.db.EncryptedAccountDbModel
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.entities.EncryptedAccountDomain
import javax.inject.Inject

class AccountMapper @Inject constructor() {
    fun mapDbToDt(accountDbModel: AccountDbModel) = AccountDataDomain (
            id = accountDbModel.id,
            name = accountDbModel.name,
            passPhrase = accountDbModel.passPhrase,
            passWord = accountDbModel.passWord,
            primary = accountDbModel.primary,
            destroyer = accountDbModel.destroyer,
            iterations = accountDbModel.iterations
    )

    fun mapDtToDb(accountDataDomain: AccountDataDomain) = AccountDbModel (
            name = accountDataDomain.name,
            passPhrase = accountDataDomain.passPhrase,
            passWord = accountDataDomain.passWord,
            primary = accountDataDomain.primary,
            destroyer = accountDataDomain.destroyer,
            iterations = accountDataDomain.iterations
    )

    fun mapEncryptedDbToDt(encryptedAccountDbModel: EncryptedAccountDbModel) = EncryptedAccountDomain (
            id= encryptedAccountDbModel.id,
            name = encryptedAccountDbModel.name,
            passPhrase = encryptedAccountDbModel.passPhrase,
            passWord = encryptedAccountDbModel.passWord,
            destroyer = encryptedAccountDbModel.destroyer,
            iv = encryptedAccountDbModel.iv,
            encrypted = encryptedAccountDbModel.encrypted,
            primary = encryptedAccountDbModel.primary,
            iterations = encryptedAccountDbModel.iterations
    )

    fun mapEncryptedDtToDn(encryptedAccountDomain: EncryptedAccountDomain) = EncryptedAccountDbModel (
            id= encryptedAccountDomain.id,
            name = encryptedAccountDomain.name,
            passPhrase = encryptedAccountDomain.passPhrase,
            passWord = encryptedAccountDomain.passWord,
            destroyer = encryptedAccountDomain.destroyer,
            iv = encryptedAccountDomain.iv,
            encrypted = encryptedAccountDomain.encrypted,
            primary = encryptedAccountDomain.primary

    )
}