package waybilmobile.company.waybilbuyer.view.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_order_details.view.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.formatCurrency
import waybilmobile.company.waybilbuyer.model.orders.OrderItem

class TransactionDetailsAdapter(val transactionsDetailList: ArrayList<OrderItem>) :
    RecyclerView.Adapter<TransactionDetailsAdapter.TransactionsDetailsViewHolder>()
{

    fun updateList(newTransactionsDetailList: List<OrderItem>){
        transactionsDetailList.clear()
        transactionsDetailList.addAll(newTransactionsDetailList)
        notifyDataSetChanged()

    }

    class TransactionsDetailsViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionsDetailsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_order_details, parent, false)
        return TransactionsDetailsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return transactionsDetailList.size
    }

    /**
     * Use this for the specifics page
     */

    override fun onBindViewHolder(holder: TransactionsDetailsViewHolder, position: Int) {


        holder.view.product_image_listPage.visibility = View.GONE
        val transactionDetItem = transactionsDetailList[position]
        val productSummary =
            "${transactionDetItem.product.productName} \n ${transactionDetItem.product.details}"

        holder.view.order_item_name.text = productSummary
        holder.view.order_item_quantity.text = transactionDetItem.quantity.toString()
        holder.view.order_item_subtotal.text = formatCurrency(transactionDetItem.subTotal)


    }
}