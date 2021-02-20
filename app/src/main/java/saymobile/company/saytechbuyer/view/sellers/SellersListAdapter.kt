package saymobile.company.saytechbuyer.view.sellers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_seller.view.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.model.user.User

class SellersListAdapter (val sellersList: ArrayList<User>) : RecyclerView.Adapter<SellersListAdapter.SellersListViewHolder>(){

    fun updateSellersList(newSellersList: List<User>){
        sellersList.clear()
        sellersList.addAll(newSellersList)
        notifyDataSetChanged()
    }

    class SellersListViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellersListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_seller, parent, false)
        return SellersListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sellersList.size
    }

    override fun onBindViewHolder(holder: SellersListViewHolder, position: Int) {
        holder.view.seller_name.text = sellersList[position].businessName
        //on click listener here
        holder.view.setOnClickListener {
            val action = BrowseSellersFragmentDirections.actionBrowseSellersFragmentToSellerStoreFragment()
            action.businessId = sellersList[position].id
            action.businessName = sellersList[position].businessName
            Navigation.findNavController(it).navigate(action)
        }

    }

}