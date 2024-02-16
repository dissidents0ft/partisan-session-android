package network.loki.messenger

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.util.Log
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import partisan_plugin.data.crypto.AesCbcUpdated
import partisan_plugin.data.dataGenerators.GenerateRandomData
import partisan_plugin.domain.entities.CipherTextIvMac

@RunWith(AndroidJUnit4::class)
@LargeTest
class CryptoTests {

    @Test
    fun testEncryptStringDecryptString() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val coroutineScope = CoroutineScope(Dispatchers.Default)
        for (i in 0..10) {
                val seed = GenerateRandomData.generateRandomSeed(context)
                Log.w(TAG, seed)
                val salt = AesCbcUpdated.generateSalt()
                val passord = GenerateRandomData.generateRandomName()
                val keys = AesCbcUpdated.generateKeyFromPassword(passord, salt)
                val encryptedString = AesCbcUpdated.encrypt(seed, keys)
                Log.w(TAG, encryptedString.getCypherString())
                val decrypted = AesCbcUpdated.decryptString(encryptedString, keys)!!
                Log.w(TAG, decrypted)
                assertEquals(seed, decrypted)
            }
        val seed = GenerateRandomData.generateRandomSeed(context)
        Log.w(TAG,seed)
        val salt = AesCbcUpdated.generateSalt()
        val key = GenerateRandomData.generateRandomName()
        val encryptionKeys = AesCbcUpdated.generateKeyFromPassword(key,salt)
        val encryptedString = AesCbcUpdated.encrypt(seed,encryptionKeys)
        val encryptionKeyN = AesCbcUpdated.generateKeyFromPassword("fakekey",salt)
        val decrypted = AesCbcUpdated.decryptString(encryptedString,encryptionKeyN)
        assertEquals(decrypted,null)
    }

    companion object {
        private const val TAG = "CRYPTO_TES_TAG"
    }
}