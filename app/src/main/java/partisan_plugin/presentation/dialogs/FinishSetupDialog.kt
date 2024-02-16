package partisan_plugin.presentation.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.view.inputmethod.EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import network.loki.messenger.R
import network.loki.messenger.databinding.FinalSetupDialogBinding
import partisan_plugin.data.Constants
import kotlin.math.max

class FinishSetupDialog :DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogBinding = FinalSetupDialogBinding.inflate(layoutInflater)
        val dialog =  MaterialAlertDialogBuilder(requireActivity())
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface, i: Int -> dialog.cancel() }
                .setView(dialogBinding.root)
                .create()
        dialogBinding.setupPrefix.imeOptions = EditorInfo.IME_ACTION_DONE or IME_FLAG_NO_PERSONALIZED_LEARNING //setting up incognito keyboard
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val prefix =  dialogBinding.setupPrefix.text.toString()
                val requestKey = arguments?.getString(ARG_REQUEST_KEY)?: SETUP
                parentFragmentManager.setFragmentResult(requestKey, bundleOf(PREFIX to prefix))
                dismiss()
            }
        }
        return dialog
    }

    companion object {
        private val TAG = SetupAccountDialog::class.simpleName
        private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
        private const val PREFIX = "PREFIX"
        const val SETUP = "SETUP"
        fun show(fragmentManager: FragmentManager, requestKey: String) {
            val fragment = FinishSetupDialog().apply { bundleOf(ARG_REQUEST_KEY to requestKey) }
            fragment.show(fragmentManager,TAG)
        }

        fun setupListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, requestKey: String, listener: (String) -> Unit) {
            fragmentManager.setFragmentResultListener(requestKey,lifecycleOwner
            ) { _, result -> listener.invoke(result.getString(PREFIX)!!) }
        }

    }
}