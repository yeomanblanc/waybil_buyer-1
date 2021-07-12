package waybilmobile.company.waybilbuyer.view.basket

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_basket.*

import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.formatCurrency
import waybilmobile.company.waybilbuyer.model.products.Product
import waybilmobile.company.waybilbuyer.viewModel.basket.BasketViewModel


class BasketFragment : Fragment(), BasketListAdapter.OnBasketClickListener {

    /**
     * implement add and remove product functionality
     */

    private lateinit var viewModel: BasketViewModel
    private var basketListAdapter = BasketListAdapter(arrayListOf(), this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_basket, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(BasketViewModel::class.java)


        close_fragment_basket.setOnClickListener {
            findNavController().navigateUp()
            viewModel.globalProductListener?.remove()
        }

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

                basketListAdapter.focusedSeller = viewModel.orderData.value?.seller?.id
                basket_recycler.visibility = View.VISIBLE
                //ASTERISK
                basketListAdapter.updateBasket(it)
                if(basketList.isNotEmpty()){
                    setData()

                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                if(it){
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
                    delivery_layout_basket.visibility = View.GONE
                    deliveryFee_layout.visibility = View.GONE
                    basketLine_split.visibility = View.GONE
                    basket_total.text = "-"
                }else{
                    no_basket_message.visibility = View.GONE
                    delivery_layout_basket.visibility = View.VISIBLE
                }
            }
        })

        viewModel.purchaseComplete.observe(viewLifecycleOwner, Observer { purchaseComplete ->
            if (purchaseComplete){
                // maybe navigate to a thank you page
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false)
            }else{
                Toast.makeText(activity, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show()
            }
        })


    }




    private fun setData(){
        val orderData = viewModel.orderData.value
        orderData?.let{
            destination_business.text = it.customer.businessName
            //If delivery is an option we make delivery options VISIBLE and non-delivery layouts
            // GONE
            //And initialize delivery switch listener
            if(it.sellerOffersDelivery){
                switchDelivery_layout.visibility = View.VISIBLE
                updateDeliveryUi(it.forDelivery)
                pickupBasket_message.visibility = View.GONE
                //Initialize switch here so that delivery fee is not added again to the total
                toggleDeliverySwitch()
            }else{
                //If order is pickup only
                switchDelivery_layout.visibility = View.GONE
                pickupBasket_message.visibility = View.VISIBLE
                destination_business.visibility = View.GONE
            }

            basket_total.text = formatCurrency(it.orderTotal)
            deliveryFee_total.text = formatCurrency(it.seller!!.deliveryCost.toDouble())

        }


    }




    private fun toggleDeliverySwitch(){
        basketDelivery_switch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateDeliveryStatus(isChecked)
        }
    }




    private fun updateDeliveryUi(delivery: Boolean){
        if(delivery){
            basketDelivery_switch.isChecked = true
            destination_business.visibility = View.VISIBLE
            deliveryFee_layout.visibility = View.VISIBLE
            basketLine_split.visibility = View.VISIBLE
            deliveryLabel_basket.setTextAppearance(R.style.DarkFontBold)
            pickupLabel_basket.setTextAppearance(R.style.GreyFont)
        }else{
            basketDelivery_switch.isChecked = false
            destination_business.visibility = View.GONE
            deliveryFee_layout.visibility = View.GONE
            basketLine_split.visibility = View.GONE
            deliveryLabel_basket.setTextAppearance(R.style.GreyFont)
            pickupLabel_basket.setTextAppearance(R.style.DarkFontBold)
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

        val dialog = builder.create()
        if(viewModel.basketList.value.isNullOrEmpty()){
            Toast.makeText(activity, getString(R.string.basket_empty), Toast.LENGTH_SHORT).show()
        }else{
            dialog.show()
            activity?.let {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        it,
                        R.color.colorAccent
                    )
                )
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    ContextCompat.getColor(
                        it,
                        R.color.colorAccent
                    )
                )
            }


        }

    }

    override fun onItemClicked(item: Product, addOrRemove: Int) {
        viewModel.updateBasket(item, addOrRemove)
    }


}
