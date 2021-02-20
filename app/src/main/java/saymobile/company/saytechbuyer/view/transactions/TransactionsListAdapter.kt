package saymobile.company.saytechbuyer.view.transactions

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_order.view.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.formatCurrency
import saymobile.company.saytechbuyer.model.orders.Order
import java.text.SimpleDateFormat

class TransactionsListAdapter (val ordersList: ArrayList<Order>) : RecyclerView.Adapter<TransactionsListAdapter.OrderViewHolder>() {


    fun updateOrdersList(newOrdersList: List<Order>){
        ordersList.clear()
        ordersList.addAll(newOrdersList)
        notifyDataSetChanged()
    }

    class OrderViewHolder(var view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        when(ordersList[position].orderStatus){
            2 -> holder.view.upper_container_order.setBackgroundResource(R.drawable.confirmed_bg)
            3 -> holder.view.upper_container_order.setBackgroundResource(R.drawable.transaction_bg)
            9 -> holder.view.upper_container_order.setBackgroundResource(R.drawable.rejected_bg)
            else -> holder.view.upper_container_order.setBackgroundResource(R.drawable.pending_bg)
        }
        val orderDate = ordersList[position].orderTimestampReceived?.toDate()

        orderDate?.let {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(it)
            holder.view.time_order.text = formattedDate

        }

        holder.view.store_name_order.text = ordersList[position].customer.businessName
        holder.view.address_order.text = ordersList[position].customer.userAddress
        //If not null execute this part of the code
        holder.view.total_order_view.text = formatCurrency(ordersList[position].orderTotal)

        holder.view.setOnClickListener {
            val action = TransactionsFragmentDirections.actionTransactionsFragmentToTransactionDetails()
            action.orderId = ordersList[position].invoiceId
            Navigation.findNavController(it).navigate(action)
        }

    }

    override fun getItemCount(): Int {
        return ordersList.size
    }
}