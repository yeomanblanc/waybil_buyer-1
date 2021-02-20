package saymobile.company.saytechbuyer.view.transactions

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.formatCurrency
import saymobile.company.saytechbuyer.model.orders.Order
import saymobile.company.saytechbuyer.viewModel.transactions.TransactionDetailViewModel
import java.text.SimpleDateFormat


class TransactionDetails : Fragment() {

    private lateinit var viewModel : TransactionDetailViewModel
    private var orderItemsAdapter = TransactionDetailsAdapter(arrayListOf())
    private var invoiceId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_details, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            invoiceId = TransactionDetailsArgs.fromBundle(it).orderId
        }

        close_fragment_orderDetails.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProviders.of(this).get(TransactionDetailViewModel::class.java)

        order_details_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderItemsAdapter
        }



        observeViewModels()
        viewModel.retrieveOrderItems(invoiceId)


    }

    fun observeViewModels() {
        viewModel.focusedOrder.observe(viewLifecycleOwner, Observer { focusedOrder ->
            focusedOrder?.let {
                setBannerData(focusedOrder)
                orderItemsAdapter.submitOrderItems(focusedOrder.productsOrderList!!)
            }
        })
    }


    @SuppressLint("SimpleDateFormat")
    fun setBannerData(focusedOrder: Order){
        when(focusedOrder.orderStatus){
            1 -> upper_container_orderDets.setBackgroundResource(R.drawable.pending_bg)
            2 -> upper_container_orderDets.setBackgroundResource(R.drawable.confirmed_bg)
            3 -> upper_container_orderDets.setBackgroundResource(R.drawable.transaction_bg)
            9 -> upper_container_orderDets.setBackgroundResource(R.drawable.rejected_bg)
        }
        val orderDate = focusedOrder.orderTimestampReceived?.toDate()
        orderDate?.let {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(it)
            time_orderContextDets.text = formattedDate

        }

        store_name_orderContextDets.text = focusedOrder.customer.businessName
        address_orderContextDets.text = focusedOrder.customer.userAddress
        total_order_viewDets.text = formatCurrency(focusedOrder.orderTotal)
    }


}