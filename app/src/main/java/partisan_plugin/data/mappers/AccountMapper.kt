package partisan_plugin.data.mappers

import partisan_plugin.data.db.AccountDbModel
import partisan_plugin.data.db.EncryptedAccountDbModel
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.entities.EncryptedAccountDomain
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

    fun mapDtToDb(accountDataDomain: AccountDataDomain) = AccountDbModel (
            passPhrase = accountDataDomain.passPhrase,
            passWord = accountDataDomain.passWord,
            primary = accountDataDomain.primary,
            destroyer = accountDataDomain.destroyer,
            pmm = accountDataDomain.memory
    )

    fun mapEncryptedDbToDt(encryptedAccountDbModel: EncryptedAccountDbModel) = EncryptedAccountDomain (
            id= encryptedAccountDbModel.id,
            passPhrase = encryptedAccountDbModel.passPhrase,
            passWord = encryptedAccountDbModel.passWord,
            destroyer = encryptedAccountDbModel.destroyer,
            iv = encryptedAccountDbModel.iv,
            salt = encryptedAccountDbModel.salt,
            hash = encryptedAccountDbModel.hash,
            encrypted = encryptedAccountDbModel.encrypted,
            primary = encryptedAccountDbModel.primary,
            memory = encryptedAccountDbModel.memory
    )

    fun mapEncryptedDtToDn(encryptedAccountDomain: EncryptedAccountDomain) = EncryptedAccountDbModel (
            id= encryptedAccountDomain.id,
            passPhrase = encryptedAccountDomain.passPhrase,
            passWord = encryptedAccountDomain.passWord,
            destroyer = encryptedAccountDomain.destroyer,
            iv = encryptedAccountDomain.iv,
            salt = encryptedAccountDomain.salt,
            hash = encryptedAccountDomain.hash,
            encrypted = encryptedAccountDomain.encrypted,
            primary = encryptedAccountDomain.primary
    )
}