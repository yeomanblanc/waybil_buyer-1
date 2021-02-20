package saymobile.company.saytechbuyer.view.basket

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_basket.*
import kotlinx.android.synthetic.main.item_basket.*

import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.formatCurrency
import saymobile.company.saytechbuyer.model.orders.Order
import saymobile.company.saytechbuyer.model.orders.OrderItem
import saymobile.company.saytechbuyer.model.products.Product
import saymobile.company.saytechbuyer.viewModel.basket.BasketViewModel


class BasketFragment : Fragment() {

    /**
     * implement add and remove product functionality
     */

    private lateinit var viewModel: BasketViewModel
    private var basketListAdapter = BasketListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_basket, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        close_fragment_basket.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProviders.of(this).get(BasketViewModel::class.java)
        basket_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = basketListAdapter
        }

        buyButton_basket.setOnClickListener {
            completePurchase()
        }

        cancelButton_basket.setOnClickListener {
            findNavController().navigateUp()
        }

        observeViewModels()
        viewModel.refresh()


    }

    private fun observeViewModels(){
        viewModel.basketList.observe(viewLifecycleOwner, Observer { basketList ->
            basketList?.let {

                basketListAdapter.focusedSeller = viewModel.orderData.value?.sellerUid
                basket_recycler.visibility = View.VISIBLE
                basketListAdapter.updateBasket(basketList)
                setTotal()
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                if(loading){
                    progressBar_basket.visibility = View.VISIBLE
                }else{
                    progressBar_basket.visibility = View.GONE
                }
            }
        })

        viewModel.noBasket.observe(viewLifecycleOwner, Observer { noBasket ->
            noBasket?.let {
                if(noBasket){
                    basket_recycler.visibility = View.GONE
                    no_basket_message.visibility = View.VISIBLE
                }else{
                    no_basket_message.visibility = View.GONE
                }
            }
        })

        viewModel.purchaseComplete.observe(viewLifecycleOwner, Observer { purchaseComplete ->
            if (purchaseComplete){
                // maybe navigate to a thank you page
                findNavController().navigateUp()
            }else{
                Toast.makeText(activity, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show()
            }
        })

        basketListAdapter.itemToUpdate.observe(viewLifecycleOwner, Observer { itemToUpdate ->
            itemToUpdate?.let {
                viewModel.updateBasket(itemToUpdate)
            }
        })

        basketListAdapter.itemToDelete.observe(viewLifecycleOwner, Observer { itemToDelete ->
            itemToDelete?.let {
                viewModel.deleteFromBasket(itemToDelete)
            }
        })


    }

    /**
     * Total should be the orderTotal in Order class
     * Set that variable to var and update in BasketViewModel and
     * SellerStoreViewModel
     */
    private fun setTotal(){
        if(viewModel.orderData.value?.orderTotal != null){
            val orderTotal = viewModel.orderData.value?.orderTotal
            basket_total.text = orderTotal?.let { formatCurrency(it) }
        }else{
            basket_total.text = "-"
        }

    }

    private fun completePurchase(){
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(getString(R.string.confirm_order_dialog) ).setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }.setPositiveButton(getString(R.string.ok)) { _, _ ->
//            /**
//             * This is here to ensure that the purchase data being sent is not old data but the
//             */
//            val liveOrder = viewModel.orderData
//            liveOrder.observe(viewLifecycleOwner, Observer { orderData ->
//                viewModel.sendOrder()
//            })
            viewModel.executePurchase()
        }
        if(viewModel.basketList.value.isNullOrEmpty()){
            Toast.makeText(activity, getString(R.string.basket_empty), Toast.LENGTH_SHORT).show()
        }else{
            builder.show()


        }

    }

}
