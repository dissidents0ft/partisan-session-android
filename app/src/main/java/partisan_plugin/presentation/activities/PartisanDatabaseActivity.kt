package partisan_plugin.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import network.loki.messenger.R
import network.loki.messenger.databinding.ActivityPartisanDatabaseBinding
import org.thoughtcrime.securesms.onboarding.LinkDeviceActivity
import org.thoughtcrime.securesms.util.push
import partisan_plugin.data.repositories.PreferencesRepository
import partisan_plugin.domain.AppStartAction
import partisan_plugin.domain.entities.AccountDataDomain
import partisan_plugin.domain.usecases.accountsDatabase.AddUnencryptedAccountUseCase
import partisan_plugin.domain.usecases.accountsDatabase.DeleteAccountUseCase
import partisan_plugin.domain.usecases.accountsDatabase.EncryptDatabaseUseCase
import partisan_plugin.domain.usecases.accountsDatabase.GetUnencryptedDataUseCase
import partisan_plugin.domain.usecases.accountsDatabase.UpdateUnencryptedAccountUseCase
import partisan_plugin.presentation.adapters.MyAccountAdapter
import partisan_plugin.presentation.dialogs.SetupAccountDialog
import javax.inject.Inject

@AndroidEntryPoint
class PartisanDatabaseActivity : AppCompatActivity() {

    @Inject
    lateinit var addUnencryptedAccountUseCase: AddUnencryptedAccountUseCase

    @Inject
    lateinit var deleteAccountUseCase: DeleteAccountUseCase

    @Inject
    lateinit var encryptDatabaseUseCase: EncryptDatabaseUseCase

    @Inject
    lateinit var updateUnencryptedAccountUseCase: UpdateUnencryptedAccountUseCase

    @Inject
    lateinit var getUnencryptedDataUseCase: GetUnencryptedDataUseCase

    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var myAccountAdapter: MyAccountAdapter

    lateinit var binding: ActivityPartisanDatabaseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partisan_database)
        binding = ActivityPartisanDatabaseBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupFinish()
        setupFAB()
        setupRecyclerView()
    }

    private fun MyAccountAdapter.setRecyclerViewListeners() {
        onDeleteItemClickListener = { id -> coroutineScope.launch {  deleteAccountUseCase(id) } }
        onEditItemClickListener = {
            SetupAccountDialog.show(supportFragmentManager,SetupAccountDialog.UPDATE,it.passWord,it.name,it.passPhrase,it.primary,it.destroyer,it.iterations)
            SetupAccountDialog.setupListener(supportFragmentManager, this@PartisanDatabaseActivity, SetupAccountDialog.UPDATE) {
                name, passPhrase, pass, iterations, isPrimary, isDestroyer ->
                coroutineScope.launch {
                    updateUnencryptedAccountUseCase(AccountDataDomain(it.id, name, passPhrase, pass, primary = isPrimary, destroyer = isDestroyer, iterations = iterations))
                }
            }
        }
    }

    private fun setupRecyclerView() {
        with(binding.items) {
            layoutManager = LinearLayoutManager(context)
            myAccountAdapter.setRecyclerViewListeners()
            adapter = myAccountAdapter
        }
        coroutineScope.launch {
            getUnencryptedDataUseCase().collect{
                myAccountAdapter.submitList(it)
            }
        }
    }

    private fun setupFinish() {
        binding.finishButton.setOnClickListener {
            finishSetup()
        }
    }

    private fun setupFAB() {
        binding.floatingActionButton.setOnClickListener {
            SetupAccountDialog.show(supportFragmentManager, SetupAccountDialog.ADD)
            SetupAccountDialog.setupListener(supportFragmentManager, this,SetupAccountDialog.ADD) {

                name, passPhrase, pass, iterations, isPrimary, isDestroyer ->
                coroutineScope.launch {
                    addUnencryptedAccountUseCase(name, passPhrase, pass, isPrimary, isDestroyer, iterations)
                }
            }
        }
    }

    private fun finishSetup() {
        val intent = Intent(this, LinkDeviceActivity::class.java)
        coroutineScope.launch {
            encryptDatabaseUseCase()
            PreferencesRepository.setAppStartAction(applicationContext, AppStartAction.START_ENTER_PRIMARY_PHRASE)
            push(intent)
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

}