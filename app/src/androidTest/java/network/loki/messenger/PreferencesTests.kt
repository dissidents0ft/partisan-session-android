package network.loki.messenger

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.session.libsignal.utilities.Log
import partisan_plugin.data.crypto.AesCbcUpdated
import partisan_plugin.data.repositories.PreferencesRepository
import partisan_plugin.domain.entities.AppStartAction

@RunWith(AndroidJUnit4::class)
class PreferencesTests {
    @Test
    fun enumPreferencesSurvivalOnDataClear() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (action in AppStartAction.values()) {
            PreferencesRepository.setAppStartAction(context,action)
            org.thoughtcrime.securesms.ApplicationContext.getInstance(context).clearAllData(true)
            val gotAction = PreferencesRepository.getAppStartAction(context)
            assertEquals(action,gotAction)
        }
    }

}