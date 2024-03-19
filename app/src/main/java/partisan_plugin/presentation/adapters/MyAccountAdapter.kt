package partisan_plugin.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import network.loki.messenger.R
import network.loki.messenger.databinding.SessionAccountItemBinding
import partisan_plugin.domain.entities.AccountDataDomain
import javax.inject.Inject

class MyAccountAdapter @Inject constructor(
        diffCallback: MyAccountAdapterDiffCallback,
) : ListAdapter<AccountDataDomain, MyAccountViewHolder>(diffCallback) {

    var onEditItemClickListener: ((AccountDataDomain) -> Unit)? = null
    var onDeleteItemClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAccountViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SessionAccountItemBinding.inflate(inflater, parent, false)
        return MyAccountViewHolder(binding)
    }

    //Наполнение строк Recycler View содержимым
    override fun onBindViewHolder(holder: MyAccountViewHolder, position: Int) {
        val account = getItem(position)
        with(holder.binding) {
            password.text = account.passWord
            seedTextView.text = account.passPhrase
            val context = root.context
            accountType.text = if (account.destroyer) {
                context.getString(R.string.account_type,context.getString(R.string.destroyer_account_type))
            } else if (account.primary) {
                context.getString(R.string.account_type,context.getString(R.string.primary_account_type))
            } else {
                context.getString(R.string.account_type,context.getString(R.string.hidden_account_type))
            }

            deleteButton.setOnClickListener {
                onDeleteItemClickListener?.invoke(account.index)
            }
            editButton.setOnClickListener {
                onEditItemClickListener?.invoke(account)
            }
        }
    }

}