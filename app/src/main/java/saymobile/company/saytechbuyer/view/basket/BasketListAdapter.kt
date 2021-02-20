package saymobile.company.saytechbuyer.view.basket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_dashboard.view.*
import kotlinx.android.synthetic.main.item_basket.view.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.formatCurrency
import saymobile.company.saytechbuyer.getProgressDrawable
import saymobile.company.saytechbuyer.loadImage
import saymobile.company.saytechbuyer.model.orders.OrderItem
import saymobile.company.saytechbuyer.model.products.Product

class BasketListAdapter (val basketList: ArrayList<OrderItem>): RecyclerView.Adapter<BasketListAdapter.BasketListViewHolder>() {


    val itemToUpdate = MutableLiveData<OrderItem>()
    val itemToDelete = MutableLiveData<Product>()
    var focusedSeller: String? = ""

    fun updateBasket(newBasketList: List<OrderItem>){
        basketList.clear()
        basketList.addAll(newBasketList)
        notifyDataSetChanged()
    }

    class BasketListViewHolder(var view: View) :  RecyclerView.ViewHolder(view)

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
        var quantity = basketItem.quantity

        val gsReference =
            FirebaseStorage.getInstance().reference.child("/$focusedSeller" +
                    "/inventory//${basketItem.product.skuNumber}/${basketItem.product.imageRef}.jpeg")

        val productNameAndDets = "${basketItem.product.productName}\n ${basketItem.product.details}"


        holder.view.add_product_basket.setOnClickListener {
            quantity += 1
            val newOrderItem = OrderItem(basketItem.product, quantity)
            itemToUpdate.value = newOrderItem
        }

        holder.view.remove_product_basket.setOnClickListener {
            if(quantity > 1){
                quantity -= 1
                val newOrderItem = OrderItem(basketItem.product, quantity)
                itemToUpdate.value = newOrderItem

            } else if(quantity == 1){
                itemToDelete.value = basketItem.product
            }
        }

        holder.view.product_name.text = productNameAndDets
        holder.view.product_quantity.text = basketItem.quantity.toString()

        holder.view.total_price.text = formatCurrency(basketItem.subTotal)
//        holder.view.total_price.text = String.format("%.2f", basketItem.subTotal)

        holder.view.productImage_basket.apply {
            clipToOutline = true
        }

        holder.view.productImage_basket.loadImage(gsReference, getProgressDrawable(holder.view
            .productImage_basket.context))




    }
}