package partisan_plugin.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import partisan_plugin.domain.entities.AccountDataDomain
import javax.inject.Inject

class MyAccountAdapterDiffCallback @Inject constructor(): DiffUtil.ItemCallback<AccountDataDomain>() {
    override fun areItemsTheSame(oldItem: AccountDataDomain, newItem: AccountDataDomain): Boolean =
            oldItem.id == newItem.id


    override fun areContentsTheSame(oldItem: AccountDataDomain, newItem: AccountDataDomain): Boolean =
            oldItem == newItem
}