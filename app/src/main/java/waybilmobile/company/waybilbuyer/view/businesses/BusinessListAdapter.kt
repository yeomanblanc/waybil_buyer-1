package waybilmobile.company.waybilbuyer.view.businesses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_business.view.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.model.userbusiness.UserBusiness
import waybilmobile.company.waybilbuyer.setFocusedBusiness

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
        val view = inflater.inflate(R.layout.item_business, parent, false)
        return BusinessListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return businessList.size
    }

    override fun onBindViewHolder(holder: BusinessListViewHolder, position: Int) {
        //User a seller_item which is why seller_name is the TextView mentioned below
        holder.view.businessName_businessFragment.text = businessList[position].businessName
        holder.view.setOnClickListener {
            val action = BusinessFragmentDirections.actionBusinessFragmentToBusinessDetailsFragment()
            Navigation.findNavController(it).navigate(action)
            setFocusedBusiness(businessList[position])
        }

    }

}