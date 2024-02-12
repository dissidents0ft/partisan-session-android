package partisan_plugin.presentation.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import network.loki.messenger.R
import network.loki.messenger.databinding.AddAccountDialogBinding
import partisan_plugin.data.Constants

class SetupAccountDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBinding = AddAccountDialogBinding.inflate(layoutInflater)

        val dialog =  MaterialAlertDialogBuilder(requireActivity())
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface, i: Int -> dialog.cancel() }
                .setView(dialogBinding.root)
                .create()
        with(dialogBinding) {
            destroyerAccount.isChecked = arguments?.getBoolean(IS_DESTROYER)?:false
            primaryAccount.isChecked = arguments?.getBoolean(IS_PRIMARY)?:false
            arguments?.getString(NAME)?.let {
                enterName.setText(it)
            }
            arguments?.getString(PASSPHRASE)?.let {
                enterPassphrase.setText(it)
            }
            arguments?.getString(PASS)?.let {
                password.setText(it)
            }
            enterIterationsNumber.setText((arguments?.getInt(ITERATIONS)?: 600000).toString())
        }
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val passphrase = dialogBinding.enterPassphrase.text.toString()
                val pass = dialogBinding.password.text.toString()
                val name = dialogBinding.enterName.text.toString()
                //val iterations = dialogBinding.enterIterationsNumber.text.toString().toInt()
                val primary = dialogBinding.primaryAccount.isChecked
               // val destroyer = dialogBinding.destroyerAccount.isChecked
                val requestKey = arguments?.getString(ARG_REQUEST_KEY)?: ADD
                parentFragmentManager.setFragmentResult(requestKey, bundleOf(PASSPHRASE to passphrase, PASS to pass, NAME to name, ITERATIONS to Constants.DEFAULT_ITERATIONS, IS_PRIMARY to primary, IS_DESTROYER to false))
                dismiss()
            }
        }

        return dialog
    }

    companion object {
        private val TAG = SetupAccountDialog::class.simpleName
        const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
        private const val PASS = "PASS"
        private const val IS_PRIMARY = "IS_PRIMARY"
        private const val IS_DESTROYER = "IS_DESTROYER"
        private const val ITERATIONS = "ITERATIONS"
        private const val NAME = "NAME"
        const val UPDATE = "UPDATE"
        const val ADD = "ADD"
        private const val PASSPHRASE = "PASSPHRASE"
        fun show(fragmentManager: FragmentManager, requestKey: String, pass: String?=null,name: String?=null, passphrase: String?=null, isPrimary: Boolean=false,isDestroyer: Boolean=false,iterations: Int = Constants.DEFAULT_ITERATIONS) {
            val fragment = SetupAccountDialog().apply {
                arguments = bundleOf(PASS to pass,NAME to name, PASSPHRASE to passphrase, IS_DESTROYER to isDestroyer, IS_PRIMARY to isPrimary, ITERATIONS to iterations, ARG_REQUEST_KEY to requestKey)
            }
            fragment.show(fragmentManager,TAG)
        }

        fun setupListener(fragmentManager: FragmentManager,  lifecycleOwner: LifecycleOwner, requestKey: String, listener: (String,String,String, Int,Boolean, Boolean) -> Unit) {
            fragmentManager.setFragmentResultListener(requestKey,lifecycleOwner
            ) { _, result -> listener.invoke(result.getString(NAME)!!,result.getString(PASSPHRASE)!!,result.getString(PASS)!!,result.getInt(ITERATIONS), result.getBoolean(IS_PRIMARY), result.getBoolean(IS_DESTROYER)) }
        }

    }
}