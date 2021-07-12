package waybilmobile.company.waybilbuyer.view.transactions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.formatCurrency
import waybilmobile.company.waybilbuyer.getFocusedOrder
import waybilmobile.company.waybilbuyer.resetFocusedOrder
import java.text.SimpleDateFormat


class TransactionDetailsFragment : Fragment() {

    private var transactionDetailsAdapter = TransactionDetailsAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_transactionDetails.setOnClickListener {
            resetFocusedOrder()
            findNavController().navigateUp()
        }

        transactionSpecifics_Recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionDetailsAdapter
        }


        val focusedOrder = getFocusedOrder()
        println(focusedOrder)
        focusedOrder?.let {

            val invoiceNumber = "#${it.invoiceNumber}"
            it.productsOrderList?.let { it1 -> transactionDetailsAdapter.updateList(it1) }
            customer_name_transactionSpecifics.text = it.customer.businessName
            total_transactionSpecifics.text = formatCurrency(it.orderTotal)
            header_transactionSpecifics.text = invoiceNumber
            invoice_number_transactionSpecifics.text = invoiceNumber
            if(it.forDelivery){
                deliverCost_layout_transactionSpecifics.visibility = View.VISIBLE
                deliveryCost_transactionSpecifics.text = formatCurrency(it.seller!!.deliveryCost.toDouble())
            }else{
                deliverCost_layout_transactionSpecifics.visibility = View.GONE
            }

        }

        focusedOrder!!.orderTimestampDelivered?.let {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy")
                .format(it.toDate())
            order_date_transactionSpecifics.text = formattedDate
        }

    }


}