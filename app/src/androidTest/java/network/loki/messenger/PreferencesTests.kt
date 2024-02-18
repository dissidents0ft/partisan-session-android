package network.loki.messenger

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import partisan_plugin.data.crypto.PartisanEncryption
import partisan_plugin.data.dataGenerators.GenerateRandomData
import partisan_plugin.data.repositories.PreferencesRepository
import partisan_plugin.domain.entities.AppStartAction

@RunWith(AndroidJUnit4::class)
class PreferencesTests {


    @Test
    fun enumPreferencesSurvivalOnDataClear() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferencesRepository = PreferencesRepository(context)
        for (action in AppStartAction.values()) {
            preferencesRepository.setAppStartAction(action)
            org.thoughtcrime.securesms.ApplicationContext.getInstance(context).clearAllData(true)
            val gotAction = preferencesRepository.getAppStartAction()
            assertEquals(action,gotAction)
        }
    }

    @Test
    fun seedPreferencesClearAfterGetting() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val encryption = PartisanEncryption(context)
        val seed = GenerateRandomData.generateRandomSeed(context)
        encryption.setEncryptedSeed(seed)
        val comparison = encryption.getEncryptedSeedAndClear()
        assertEquals(comparison,seed)
        assertEquals(null,encryption.getEncryptedSeedAndClear())
    }

}