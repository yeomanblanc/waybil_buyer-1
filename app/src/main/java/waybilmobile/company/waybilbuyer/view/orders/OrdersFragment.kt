package waybilmobile.company.waybilbuyer.view.orders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_orders.*

import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.viewModel.orders.TransactionsViewModel

/**
 * A simple [Fragment] subclass.
 */
class OrdersFragment : Fragment() {

    private lateinit var viewModel: TransactionsViewModel
    private var transactionsListAdapter = OrdersListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_orders.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProviders.of(this).get(TransactionsViewModel::class.java)
        orders_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionsListAdapter
        }

        orders_filters.setOnCheckedChangeListener { _, checkedId ->
            viewModel.refresh(
                when(checkedId){
                    R.id.radio_pending_orders -> 1
                    else -> 2

                }
            )
        }

        radio_pending_orders.isChecked = true
        viewModel.refresh(1)
        observeOrdersViewModel()

    }

    private fun observeOrdersViewModel(){
        viewModel.listOfOrders.observe(viewLifecycleOwner, Observer { orders ->
            if(orders.isEmpty()){
                noOrders_message.visibility = View.VISIBLE
                orders_recycler.visibility = View.GONE
            }else{
                orders?.let {
                    orders_recycler.visibility = View.VISIBLE
                    noOrders_message.visibility = View.GONE
                    orders_recycler_error.visibility = View.GONE
                    transactionsListAdapter.updateOrdersList(orders)
                }
            }

        })

        viewModel.ordersLoadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let{
                orders_recycler_error.visibility = if(it) View.VISIBLE else View.GONE
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                orders_progress_bar.visibility = if(it) View.VISIBLE else View.GONE
            }
        })
    }




}
