package network.loki.messenger

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.util.Log
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import partisan_plugin.data.crypto.PartisanEncryption
import partisan_plugin.data.dataGenerators.GenerateRandomData

@RunWith(AndroidJUnit4::class)
@LargeTest
class CryptoTests {

    @Test
    fun testEncryptStringDecryptString() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val encryption = PartisanEncryption(context)
        for (i in 0..10) {
                val seed = GenerateRandomData.generateRandomSeed(context)
                Log.w(TAG, seed)
                val passord = GenerateRandomData.generateRandomName()
                encryption.encryptRealData(i,passord.toCharArray(),seed)
                val decrypted = encryption.decryptData(i,passord.toCharArray())
                Log.w(TAG, decrypted)
                assertEquals(seed, decrypted)
            }
        val seed = GenerateRandomData.generateRandomSeed(context)
        Log.w(TAG,seed)
        val passord = GenerateRandomData.generateRandomName()
        encryption.encryptRealData(11,passord.toCharArray(),seed)
        val decrypted = encryption.decryptData(11,"fake".toCharArray())
        assertEquals(decrypted,null)
    }

    companion object {
        private const val TAG = "CRYPTO_TES_TAG"
    }
}