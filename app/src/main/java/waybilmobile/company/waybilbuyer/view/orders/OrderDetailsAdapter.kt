package waybilmobile.company.waybilbuyer.view.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_order_details.view.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.formatCurrency
import waybilmobile.company.waybilbuyer.getProgressDrawable
import waybilmobile.company.waybilbuyer.loadImage
import waybilmobile.company.waybilbuyer.model.orders.OrderItem

class OrderDetailsAdapter (private val orderItemsList: ArrayList<OrderItem>) : RecyclerView.Adapter<OrderDetailsAdapter.TransactionItemViewHolder>(){

    var focusedSeller: String? = ""


    fun submitOrderItems(newOrderItems: List<OrderItem>){
        orderItemsList.clear()
        orderItemsList.addAll(newOrderItems)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_order_details, parent, false)
        return TransactionItemViewHolder(view)
    }

    override fun getItemCount() = orderItemsList.size

    override fun onBindViewHolder(holder: TransactionItemViewHolder, position: Int) {

        val item = orderItemsList[position]

        val gsReference =
            FirebaseStorage.getInstance().reference.child("/$focusedSeller" +
                    "/inventory//${item.product.skuNumber}/${item.product.imageRef}.jpeg")

        val productSummary =
            "${item.product.productName} \n ${item.product.details}"
        val price = formatCurrency(item.subTotal)
        holder.view.order_item_name.text = productSummary
        holder.view.order_item_quantity.text = item.quantity.toString()
        holder.view.order_item_subtotal.text = price


        holder.view.product_image_listPage.apply {
            clipToOutline = true
        }

        holder.view.product_image_listPage.loadImage(gsReference, getProgressDrawable(holder.view
            .product_image_listPage.context)
        )
    }

    class TransactionItemViewHolder(var view: View) : RecyclerView.ViewHolder(view)
}