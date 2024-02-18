package org.thoughtcrime.securesms.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import network.loki.messenger.databinding.ActivityLandingBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseActionBarActivity
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.service.KeyCachingService
import org.thoughtcrime.securesms.util.push
import org.thoughtcrime.securesms.util.setUpActionBarSessionLogo
import partisan_plugin.data.repositories.PreferencesRepository
import partisan_plugin.domain.entities.AppStartAction
import partisan_plugin.presentation.activities.PartisanDatabaseActivity
import javax.inject.Inject

@AndroidEntryPoint
class LandingActivity : BaseActionBarActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLandingBinding.inflate(layoutInflater)
        when(preferencesRepository.getAppStartAction()) {
            AppStartAction.SETUP_DATABASE -> {
                with(binding) {
                enterPartisan.visibility = View.VISIBLE
                enterPartisan.setOnClickListener { startPartisan() }
                }
            } //if partisan settings were not initialized, makes button opening settings visible and clickable
            AppStartAction.NORMAL_START -> {}  //loading Session normally
            AppStartAction.START_ENTER_PRIMARY_PHRASE, AppStartAction.START_ENTER_UNLOCKED_PHRASE -> { link() } //entering link device activity if automatical authentication with given seed required
        }
        setContentView(binding.root)
        setUpActionBarSessionLogo(true)
        with(binding) {
            fakeChatView.startAnimating()
            registerButton.setOnClickListener { register() }
            restoreButton.setOnClickListener { link() }
            linkButton.setOnClickListener { link() }
        }
        IdentityKeyUtil.generateIdentityKeyPair(this)
        TextSecurePreferences.setPasswordDisabled(this, true)
        // AC: This is a temporary workaround to trick the old code that the screen is unlocked.
        KeyCachingService.setMasterSecret(applicationContext, Object())
    }

    private fun startPartisan() {
        startActivity(Intent(this@LandingActivity, PartisanDatabaseActivity::class.java))
    }

    private fun register() {
        val intent = Intent(this, RegisterActivity::class.java)
        push(intent)
    }

    private fun link() {
        val intent = Intent(this, LinkDeviceActivity::class.java)
        push(intent)
    }
}