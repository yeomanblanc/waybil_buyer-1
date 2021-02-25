package saymobile.company.saytechbuyer.view.businesses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_seller.view.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.model.userbusiness.UserBusiness

class BusinessListAdapter (val businessList: ArrayList<UserBusiness>) :
    RecyclerView.Adapter<BusinessListAdapter.BusinessListViewHolder>() {

    fun updateBusinessList(newBusinessList: List<UserBusiness>){
        businessList.clear()
        businessList.addAll(newBusinessList)
        notifyDataSetChanged()
    }


    class BusinessListViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_seller, parent, false)
        return BusinessListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return businessList.size
    }

    override fun onBindViewHolder(holder: BusinessListViewHolder, position: Int) {
        //User a seller_item which is why seller_name is the TextView mentioned below
        holder.view.seller_name.text = businessList[position].businessName
        holder.view.setOnClickListener {
            val action = BusinessFragmentDirections.actionBusinessFragmentToBrowseSellersFragment()
            Navigation.findNavController(it).navigate(action)
        }

    }

}