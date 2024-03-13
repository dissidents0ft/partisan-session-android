package partisan_plugin.data.crypto

import android.content.Context
import androidx.core.content.edit
import at.favre.lib.armadillo.Armadillo
import dagger.hilt.android.qualifiers.ApplicationContext
import partisan_plugin.TopLevelFunctions.clear
import javax.inject.Inject

/**
 * Class for data encryption.
 */

class PartisanEncryption @Inject constructor(@ApplicationContext private val context: Context) {


    /**
     * Function that encrypts fake data. Key is randomly generated and forgot forever.
     * @param id id of file to store mock dara
     * @param seed fake Session seed to encrypt
     */
    fun encryptMockData(id: Int, seed: String) {
        val armadillo = Armadillo.create(context, PREFIX+id)
                .encryptionFingerprint(context)
                .password(null)
                .keyStretchingFunction(RandomKeyStretcher())
                .build()
        armadillo.edit {
            putString(SEED,seed)
        }
    }

    /**
     * Function that encrypts real data. Key is derived using Argon2 function.
     * @param id id of file to store mock dara
     * @param password user's password
     * @param seed Session seed to encrypt
     */
    fun encryptRealData(id: Int,password: CharArray, seed: String) {
        val armadillo = Armadillo.create(context, PREFIX+id)
                .encryptionFingerprint(context)
                .password(password)
                .keyStretchingFunction(Argon2KeyStretcher())
                .build()
        armadillo.edit {
            putString(SEED,seed)
        }
        password.clear()
    }

    /**
     * Function that decrypts data with password given by user.
     * @param id id of file to store mock dara
     * @param password user's password
     * @return decrypted data or null if decryption failed
     */
    fun decryptData(id:Int,password: CharArray): String? {
        val armadillo = Armadillo.create(context, PREFIX + id)
                .encryptionFingerprint(context)
                .password(password)
                .keyStretchingFunction(Argon2KeyStretcher())
                .build()
        return try {
            armadillo.getString(SEED,null)
        } catch (e: at.favre.lib.armadillo.SecureSharedPreferenceCryptoException) {
         null
        }
    }

    /**
     * Function to store not very sensitive data or to store sensitive data **temporarily** without user's given password. Make access to this data harder.
     * @param key key of preference
     * @param value value to put
     */
    private fun setEncryptedString(key: String,value: String?) {
        val armadillo = Armadillo.create(context, OBFUSCATED_PREFS_FILE)
                .encryptionFingerprint(context)
                .build()
        armadillo.edit {
            putString(key,value)
        }
    }

    /**
     * Function to get data encrypted without user's given password
     * @param key key of preference
     * @param defaultValue default value
     * @return decrypted string or null if decryption was unsuccessful
     */
    private fun getEncryptedString(key: String, defaultValue: String?): String? {
        val armadillo = Armadillo.create(context, OBFUSCATED_PREFS_FILE)
                .encryptionFingerprint(context)
                .build()
        return armadillo.getString(key, defaultValue)
    }

    /**
     * Function to clear data encrypted without user's given password
     * @param key key of preference to remove
     */
    private fun deleteEncryptedString(key: String) {
        val armadillo = Armadillo.create(context, OBFUSCATED_PREFS_FILE)
                .encryptionFingerprint(context)
                .build()
        armadillo.edit(commit = true) {
            remove(key)
        }
    }

    /**
     * Function for setting primary user's seed.
     */
    fun setPrimarySeed(value: String) {
        setEncryptedString(PRIMARY_SEED, value)
    }
    /**
     * Function for getting primary user's seed.
     */
    fun getPrimarySeed() : String? {
        return getEncryptedString(PRIMARY_SEED,null)
    }
    /**
     * Function for getting seed of decrypted account and immediate clearing seed from encrypted preferences
     */
    fun getEncryptedSeedAndClear(): String? {
        val string = getEncryptedString(ENCRYPTION_KEY, null)
        deleteEncryptedString(ENCRYPTION_KEY)
        return string
    }
    /**
     * Function for setting seed of decrypted account.
     */
    fun setEncryptedSeed(seed: String) {
        setEncryptedString(ENCRYPTION_KEY, seed)
    }

    /**
     * Function for getting partisan prefix.
     * @return partisan prefix or null, if prefix is absent. Prefix should be set up, if prefix is not set up this may cause crash!
     */
    fun getPartisanPrefix(): String? {
        return getEncryptedString(PARTISAN_PREFIX, null)
    }

    /**
     * Function for setting partisan prefix.
     * @param prefix partisan prefix entered by user
     */
    suspend fun setPartisanPrefix(prefix: String) {
        setEncryptedString(PARTISAN_PREFIX, prefix)
    }



    companion object {
        private const val PREFIX = "Account_"
        private const val SEED = "SEED"
        private const val OBFUSCATED_PREFS_FILE = "Obfuscated-Preferences"
        private const val PRIMARY_SEED = "PRIMARY_SEED"
        private const val ENCRYPTION_KEY = "ENCRYPTION_KEY"
        private const val PARTISAN_PREFIX = "PARTISAN_PREFIX"
    }

}