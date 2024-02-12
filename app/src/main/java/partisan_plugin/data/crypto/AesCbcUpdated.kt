package partisan_plugin.data.crypto

import android.content.Context
import android.util.Base64
import partisan_plugin.data.Constants
import partisan_plugin.data.repositories.PreferencesRepository
import partisan_plugin.domain.entities.EncryptionDataContainer
import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AesCbcUpdated {
    private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding"
    private const val CIPHER = "AES"
    private const val AES_KEY_LENGTH_BITS = 256
    private const val IV_LENGTH_BYTES = 16
    private const val PBE_SALT_LENGTH_BITS = AES_KEY_LENGTH_BITS // same size as key output
    private const val PBE_ALGORITHM = "PBKDF2withHmacSHA256"

    const val BASE64_FLAGS = Base64.NO_WRAP

    fun getSalt(context: Context): ByteArray {
        val saltPreferences = PreferencesRepository.getSalt(context)
        if (saltPreferences == null) {
            val salt = generateSalt()
            PreferencesRepository.setSalt(context,salt)
            return salt
        }
        return saltPreferences
    }


    @Throws(GeneralSecurityException::class)
    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance(CIPHER)
        keyGen.init(AES_KEY_LENGTH_BITS)
        return keyGen.generateKey()
    }

    @Throws(GeneralSecurityException::class)
    fun generateKeyFromPassword(password: String, salt: ByteArray?): SecretKey {
        //Get enough random bytes for both the AES key and the HMAC key:
        val keySpec: KeySpec = PBEKeySpec(password.toCharArray(), salt,
                Constants.DEFAULT_ITERATIONS, AES_KEY_LENGTH_BITS)
        val keyFactory = SecretKeyFactory
                .getInstance(PBE_ALGORITHM)
        val keyBytes = keyFactory.generateSecret(keySpec).encoded
        //Generate the AES key
        return SecretKeySpec(keyBytes, CIPHER)
    }


    @Throws(GeneralSecurityException::class)
    fun generateSalt(): ByteArray {
        return randomBytes(PBE_SALT_LENGTH_BITS)
    }

    @Throws(GeneralSecurityException::class)
    fun generateIv(): ByteArray {
        return randomBytes(IV_LENGTH_BYTES)
    }

    @Throws(GeneralSecurityException::class)
    private fun randomBytes(length: Int): ByteArray {
        val random = SecureRandom()
        val b = ByteArray(length)
        random.nextBytes(b)
        return b
    }


    @JvmOverloads
    @Throws(UnsupportedEncodingException::class, GeneralSecurityException::class)
    fun encrypt(plaintext: String, iv: ByteArray, secretKey: SecretKey, encoding: String? = "UTF-8"): String {
        return encrypt(plaintext.toByteArray(charset(encoding!!)),iv,secretKey)
    }


    @Throws(GeneralSecurityException::class)
    fun encrypt(plaintext: ByteArray,iv: ByteArray,key: SecretKey): String {
        val aesCipherForEncryption = Cipher.getInstance(CIPHER_TRANSFORMATION)
        aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val byteCipherText = aesCipherForEncryption.doFinal(plaintext)
        return Base64.encodeToString(byteCipherText, BASE64_FLAGS)
    }


    @JvmOverloads
    @Throws(UnsupportedEncodingException::class, GeneralSecurityException::class)
    fun decryptString(container: EncryptionDataContainer, secretKey: SecretKey, encoding: String? = "UTF-8"): String? {
        val decrypted = decrypt(container, secretKey)
        return if (decrypted==null) {
            null
        } else {
            String(decrypted, charset(encoding!!))
        }
    }

    @Throws(GeneralSecurityException::class)
    fun decrypt(container: EncryptionDataContainer, key: SecretKey): ByteArray? {
        val aesCipherForDecryption = Cipher.getInstance(CIPHER_TRANSFORMATION)
        aesCipherForDecryption.init(Cipher.DECRYPT_MODE, key,
                    IvParameterSpec(container.getEncryptedIv()))
        return try {
            aesCipherForDecryption.doFinal(container.getEncryptedData())
        } catch (e: Exception) {
            null
        }
    }
}