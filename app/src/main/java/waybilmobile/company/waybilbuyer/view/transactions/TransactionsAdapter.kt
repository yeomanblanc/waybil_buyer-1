package waybilmobile.company.waybilbuyer.view.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_transaction_details.view.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.formatCurrency
import waybilmobile.company.waybilbuyer.model.orders.Order
import waybilmobile.company.waybilbuyer.setFocusedOrder
import java.text.SimpleDateFormat

class TransactionsAdapter(private val transactionsList: ArrayList<Order>) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {


    fun updateTransactionsList(newTransactionsList: List<Order>){
        transactionsList.clear()
        transactionsList.addAll(newTransactionsList)
        notifyDataSetChanged()
    }


    class TransactionViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_transaction_details, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {

        val orderDate = transactionsList[position].orderTimestampDelivered?.toDate()

//        orderDate?.let {
//            val formattedDate = SimpleDateFormat("dd/MM/yyyy").format(it)
//            holder.view.date_transactions.text = formattedDate.toString()
//
//        }
        val invoiceNumber = "#${transactionsList[position].invoiceNumber}"
        holder.view.date_transactions.text = invoiceNumber

//        holder.view.customerName_transaction.text = transactionsList[position].customer.businessName
        holder.view.totalValue_transactions.text = formatCurrency(transactionsList[position].orderTotal)

        holder.view.setOnClickListener {
            val action = TransactionsFragmentDirections.actionTransactionsFragmentToTransactionDetailsFragment()
            Navigation.findNavController(it).navigate(action)
            setFocusedOrder(transactionsList[position])
        }
    }


    override fun getItemCount(): Int {
        return transactionsList.size
    }
}