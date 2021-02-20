package saymobile.company.saytechbuyer.view.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_order_details.view.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.formatCurrency
import saymobile.company.saytechbuyer.model.orders.OrderItem

class TransactionDetailsAdapter (val orderItemsList: ArrayList<OrderItem>) : RecyclerView.Adapter<TransactionDetailsAdapter.TransactionItemViewHolder>(){

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
        val productSum =
            "${orderItemsList[position].product.productName} \n ${orderItemsList[position].product.details}"
        val price = formatCurrency(orderItemsList[position].subTotal)
        holder.view.order_item_name.text = productSum
        holder.view.order_item_quantity.text = orderItemsList[position].quantity.toString()
        holder.view.order_item_subtotal.text = price
    }

    class TransactionItemViewHolder(var view: View) : RecyclerView.ViewHolder(view)
}