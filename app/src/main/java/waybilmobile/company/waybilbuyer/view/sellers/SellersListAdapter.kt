package waybilmobile.company.waybilbuyer.view.sellers

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_seller.view.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.getProgressDrawable
import waybilmobile.company.waybilbuyer.loadImage
import waybilmobile.company.waybilbuyer.model.user.Seller
import waybilmobile.company.waybilbuyer.setFocusedSeller

class SellersListAdapter (val sellersList: ArrayList<Seller>) :
    RecyclerView.Adapter<SellersListAdapter.SellersListViewHolder>(){

    fun updateSellersList(newSellersList: List<Seller>){
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

        val seller = sellersList[position]

        val deliveryTrue = "Entrega a negocio"
        val deliveryFalse = "Para recolectar"

        val gsReference =
            FirebaseStorage.getInstance().reference.child("/${seller.id}/profile/${seller.profileImageRef}.jpeg")

        holder.view.seller_name.text = sellersList[position].userName
        if(sellersList[position].deliveryOffered == true){
            holder.view.delivery_browseSellers.text = deliveryTrue
        }else{
            holder.view.delivery_browseSellers.text = deliveryFalse
        }

        holder.view.distance_browseSellers.text =
            String.format("%.2f km", sellersList[position].distance)

        holder.view.sellerImage_browseSellers.apply {
            clipToOutline = true
        }

        holder.view.sellerImage_browseSellers.loadImage(gsReference, getProgressDrawable(holder
            .view.sellerImage_browseSellers.context)
        )

        //on click listener here
        holder.view.setOnClickListener {
            val action = BrowseSellersFragmentDirections.actionBrowseSellersFragmentToSellerStoreFragment()
            action.forDelivery = sellersList[position].deliveryOffered!!
            setFocusedSeller(sellersList[position])
            Navigation.findNavController(it).navigate(action)
        }

        if(seller.profileImageRef != null){
            holder.view.sellerImage_browseSellers.visibility = View.VISIBLE
        }else{
            holder.view.sellerImage_browseSellers.visibility = View.GONE
        }

    }

}