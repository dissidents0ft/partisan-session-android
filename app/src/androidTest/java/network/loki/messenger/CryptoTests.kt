package network.loki.messenger

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.util.Log
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import partisan_plugin.data.crypto.AesCbcUpdated
import partisan_plugin.data.dataGenerators.GenerateRandomData
import partisan_plugin.data.repositories.PreferencesRepository
import partisan_plugin.domain.entities.EncryptionDataContainer

@RunWith(AndroidJUnit4::class)
@LargeTest
class CryptoTests {

    @Test
    fun testEncryptStringDecryptString() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var salt = AesCbcUpdated.generateSalt()
        PreferencesRepository.setSalt(context,salt)
        for (i in 0..15) {
            val seed = GenerateRandomData.generateRandomSeed(context)
            Log.w(TAG,seed)
            val key = GenerateRandomData.generateRandomName()
            salt = PreferencesRepository.getSalt(context)!!
            val iv = AesCbcUpdated.generateIv()
            val encryptionKey = AesCbcUpdated.generateKeyFromPassword(key,salt)
            val encryptedString = AesCbcUpdated.encrypt(seed,iv,encryptionKey)
            Log.w(TAG,encryptedString)
            val decrypted = AesCbcUpdated.decryptString(EncryptionDataContainer(Base64.encodeToString(iv,Base64.NO_WRAP),encryptedString),encryptionKey)!!
            Log.w(TAG,decrypted)
            assertEquals(seed,decrypted)
        }
        val seed = GenerateRandomData.generateRandomSeed(context)
        Log.w(TAG,seed)
        salt = PreferencesRepository.getSalt(context)!!
        val key = GenerateRandomData.generateRandomName()
        val iv = AesCbcUpdated.generateIv()
        val encryptionKey = AesCbcUpdated.generateKeyFromPassword(key,salt)
        val encryptedString = AesCbcUpdated.encrypt(seed,iv,encryptionKey)
        val encryptionKeyN = AesCbcUpdated.generateKeyFromPassword("fakekey",salt)
        val decrypted = AesCbcUpdated.decryptString(EncryptionDataContainer(Base64.encodeToString(iv,Base64.NO_WRAP),encryptedString),encryptionKeyN)
        assertEquals(decrypted,null)
    }

    @Test
    fun testEncryptStringRandomly() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val key = AesCbcUpdated.generateKey()
        Log.w("Encrypt_results_key",Base64.encodeToString(key.encoded, Base64.NO_WRAP))
        val iv = AesCbcUpdated.generateIv()
        Log.w("Encrypt_results_iv",Base64.encodeToString(iv, Base64.NO_WRAP))
        AesCbcUpdated.encrypt(GenerateRandomData.generateRandomSeed(context),iv,key)
    }

    @Test
    fun testStringsDecrypting() {
        val strings = listOf("wUFIROBZeA0iHGTOGMR7NA==" to "psKaskNHeaHG0xoJ8qbHSw==")
        val key = "test"
        val plaintext = "test"
        val saltString = "O+hr6SUtbad4TwHu3tP0Bn9L/o2wVkdo2TzVL1jzfdM58GEvjxkI/GGmNPivFzvn/afwMZ0/TE4D32l49TL2Vf3nc8dEhQFU0A7jpxVasB5rz3ElouHhD59o38E1EO9Vv/iJbTJL5m+S+l0kVouAMPuNIkGCFnd6U2mZW+Rur2agGr/o5JdD4NgIKsxmaL9ewBGliknl69MLV5qeieFo+oruF7nipf3Yfbin/3d40fV1yD3re79tMS6SnfNXZ4CUi14a+i8j3wNyj+ydOzfwR0COYeIFIRad3o6WigLIqfrw2fZFz0yWQrImTnkiHCEBQgvMUjAQQyY/Q1+MP0fYdQ=="
        var result: String? = null
        val keyb =             AesCbcUpdated.generateKeyFromPassword(key,Base64.decode(saltString,Base64.NO_WRAP))

        strings.forEach {
            result = AesCbcUpdated.decryptString(EncryptionDataContainer(it.second,it.first),keyb)
            if (result==plaintext) {
                return@forEach
            }
        }
        assertEquals(result,plaintext)
    }

    companion object {
        private const val TAG = "CRYPTO_TES_TAG"
    }
}