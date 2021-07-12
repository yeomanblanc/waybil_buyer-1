package waybilmobile.company.waybilbuyer.view.basket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_basket.view.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.formatCurrency
import waybilmobile.company.waybilbuyer.getProgressDrawable
import waybilmobile.company.waybilbuyer.loadImage
import waybilmobile.company.waybilbuyer.model.orders.OrderItem
import waybilmobile.company.waybilbuyer.model.products.Product

class BasketListAdapter (
    val basketList: ArrayList<OrderItem>,
    val listener: OnBasketClickListener):
    RecyclerView.Adapter<BasketListAdapter.BasketListViewHolder>() {


    val itemToUpdate = MutableLiveData<OrderItem>()
    val itemToDelete = MutableLiveData<Product>()
    var focusedSeller: String? = ""

    fun updateBasket(newBasketList: List<OrderItem>){
        basketList.clear()
        basketList.addAll(newBasketList)
        notifyDataSetChanged()
    }

    inner class BasketListViewHolder(var view: View) :  RecyclerView.ViewHolder(view),
    View.OnClickListener{
        val productName : TextView = view.product_name
        val productQuantity : TextView = view.product_quantity
        val totalPrice : TextView = view.total_price
        val productImage: ImageView = view.productImage_basket
        private val addProductBasket: TextView = view.add_product_basket
        private val removeProductBasket: TextView = view.remove_product_basket

        init {
            addProductBasket.setOnClickListener(this)
            removeProductBasket.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if(v == addProductBasket){
                if(position != RecyclerView.NO_POSITION){
                    listener.onItemClicked(basketList[position].product, 1)
                }
            }else if(v == removeProductBasket){
                if(position != RecyclerView.NO_POSITION){
                    listener.onItemClicked(basketList[position].product, -1)
                }
            }
        }
    }

    interface OnBasketClickListener{
        fun onItemClicked(item: Product, addOrRemove: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasketListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_basket, parent, false)
        return BasketListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return basketList.size
    }

    override fun onBindViewHolder(holder: BasketListViewHolder, position: Int) {


        val basketItem = basketList[position]

        val gsReference =
            FirebaseStorage.getInstance().reference.child("/$focusedSeller" +
                    "/inventory//${basketItem.product.skuNumber}/${basketItem.product.imageRef}.jpeg")

        val productNameAndDets = "${basketItem.product.productName}\n ${basketItem.product.details}"

        holder.productName.text = productNameAndDets
        holder.productQuantity.text = basketItem.quantity.toString()

        holder.totalPrice.text = formatCurrency(basketItem.subTotal)
//        holder.view.total_price.text = String.format("%.2f", basketItem.subTotal)

        holder.productImage.apply {
            clipToOutline = true
        }

        holder.productImage.loadImage(gsReference, getProgressDrawable(holder.view
            .productImage_basket.context))




    }
}