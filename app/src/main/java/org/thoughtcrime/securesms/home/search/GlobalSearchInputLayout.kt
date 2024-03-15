package org.thoughtcrime.securesms.home.search

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.loki.messenger.databinding.ViewGlobalSearchInputBinding
import org.thoughtcrime.securesms.ApplicationContext
import partisan_plugin.TopLevelFunctions.removePrefix
import partisan_plugin.TopLevelFunctions.startsWith
import partisan_plugin.TopLevelFunctions.trim
import partisan_plugin.data.Constants
import partisan_plugin.data.crypto.PartisanEncryption
import partisan_plugin.data.repositories.PreferencesRepository
import partisan_plugin.domain.entities.AppStartAction
import partisan_plugin.domain.usecases.accountsDatabase.DecryptItemUseCase
import javax.inject.Inject

@AndroidEntryPoint
class GlobalSearchInputLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs),
        View.OnFocusChangeListener,
        View.OnClickListener,
        TextWatcher, TextView.OnEditorActionListener {

    @Inject
    lateinit var decryptItemUseCase: DecryptItemUseCase
    @Inject
    lateinit var coroutineScope: CoroutineScope
    @Inject
    lateinit var partisanEncryption: PartisanEncryption
    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private val prefix by lazy { partisanEncryption.getPartisanPrefix()?.toCharArray()!! }

    var binding: ViewGlobalSearchInputBinding = ViewGlobalSearchInputBinding.inflate(LayoutInflater.from(context), this, true)

    var listener: GlobalSearchInputLayoutListener? = null

    private val _query = MutableStateFlow<CharSequence?>(null)
    val query: StateFlow<CharSequence?> = _query

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.searchInput.onFocusChangeListener = this
        binding.searchInput.addTextChangedListener(this)
        binding.searchInput.setOnEditorActionListener(this)
        binding.searchCancel.setOnClickListener(this)
        binding.searchClear.setOnClickListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v === binding.searchInput) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!hasFocus) {
                imm.hideSoftInputFromWindow(windowToken, 0)
            } else {
                imm.showSoftInput(v, 0)
            }
            listener?.onInputFocusChanged(hasFocus)
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (v === binding.searchInput && actionId == EditorInfo.IME_ACTION_SEARCH) {
            val text = query.value?.toList()?.toCharArray()
            if (text!=null && text.startsWith(prefix)) {
                coroutineScope.launch {
                    val key = text.removePrefix(prefix).trim()
                    checkPassword(key)
                }
            }
            binding.searchInput.clearFocus()
            return true
        }
        return false
    }

    private suspend fun checkPassword(key: CharArray) {
        if (key.isEmpty()) { //if no password provided by user, clearing data and entering primary account
            preferencesRepository.setAppStartAction(AppStartAction.START_ENTER_PRIMARY_PHRASE)
            context.cacheDir.deleteRecursively()
            ApplicationContext.getInstance(context).clearAllData(false)
        } else if (decryptItemUseCase(key, Constants.DEFAULT_MEMORY)) { //if user provided password and this password decrypted secret account, clearing data and entering secret account
            preferencesRepository.setAppStartAction(AppStartAction.START_ENTER_UNLOCKED_PHRASE)
            context.cacheDir.deleteRecursively()
            ApplicationContext.getInstance(context).clearAllData(false)
        }
    }

    override fun onClick(v: View?) {
        if (v === binding.searchCancel) {
            clearSearch(true)
        } else if (v === binding.searchClear) {
            clearSearch(false)
        }
    }

    fun clearSearch(clearFocus: Boolean) {
        binding.searchInput.text = null
        if (clearFocus) {
            binding.searchInput.clearFocus()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        _query.value = s?.toString()
    }

    interface GlobalSearchInputLayoutListener {
        fun onInputFocusChanged(hasFocus: Boolean)
    }

}