package waybilmobile.company.waybilbuyer.view.orders

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_order_details.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.formatCurrency
import waybilmobile.company.waybilbuyer.model.orders.Order
import waybilmobile.company.waybilbuyer.utils.CustomSupportMapFragment
import waybilmobile.company.waybilbuyer.viewModel.orders.TransactionDetailViewModel
import java.text.SimpleDateFormat
import java.util.*


class OrderDetails : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel : TransactionDetailViewModel
    private var orderItemsAdapter = OrderDetailsAdapter(arrayListOf())
    private lateinit var mMap: GoogleMap
    private var orderId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderDetails_scrollView.requestDisallowInterceptTouchEvent(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.orderDetails_map) as CustomSupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.setListener(object: CustomSupportMapFragment.OnTouchListener {
            override fun onTouch() {
                orderDetails_scrollView.requestDisallowInterceptTouchEvent(true)
            }
        })

        arguments?.let {
            orderId = OrderDetailsArgs.fromBundle(it).orderId
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
        viewModel.retrieveOrderItems(orderId)


    }

    private fun observeViewModels() {
        viewModel.focusedOrder.observe(viewLifecycleOwner, Observer { focusedOrder ->
            focusedOrder?.let {
                val latitude = focusedOrder.seller!!.geoLocation!!.latitude
                val longitude = focusedOrder.seller.geoLocation!!.longitude
                setFragmentData(focusedOrder)
                orderItemsAdapter.focusedSeller = it.seller!!.id
                orderItemsAdapter.submitOrderItems(focusedOrder.productsOrderList!!)
                if(focusedOrder.forDelivery){
                    dummyMap_frameLayout.visibility = View.GONE
                    destination_info_button.visibility = View.GONE
                    deliveryFeeLayout_orderDets.visibility = View.VISIBLE
                    deliveryFee_orderDets.text = formatCurrency(focusedOrder.seller.deliveryCost.toDouble())
                    //If buyer has to travel to pick up product show map and button to get
                // directions.
                }else if(!focusedOrder.forDelivery && focusedOrder.orderStatus == 2){
                    dummyMap_frameLayout.visibility = View.VISIBLE
                    destination_info_button.visibility = View.VISIBLE
                    val customerLocation = LatLng(latitude, longitude)
                    mMap.addMarker(MarkerOptions().position(customerLocation).title(focusedOrder.seller.userName))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 15f))

                }
            }
        })
    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    fun setFragmentData(focusedOrder: Order){
        val deliveryTrue = getString(R.string.delivery)
        val deliveryFalse = getString(R.string.for_pickup)
        val latitude = focusedOrder.seller!!.geoLocation!!.latitude
        val longitude = focusedOrder.seller.geoLocation!!.longitude

        when(focusedOrder.orderStatus){
            1 -> upper_container_orderDets.setBackgroundResource(R.drawable.pending_bg)
            2 -> {upper_container_orderDets.setBackgroundResource(R.drawable.confirmed_bg)
            orderPin_instructions.visibility = View.VISIBLE
            orderPin_layout.visibility = View.VISIBLE
            orderDetails_fragment_header.text = "#${focusedOrder.invoiceNumber}"}
            3 -> upper_container_orderDets.setBackgroundResource(R.drawable.transaction_bg)
            9 -> upper_container_orderDets.setBackgroundResource(R.drawable.rejected_bg)
        }
        val orderDate = focusedOrder.orderTimestampReceived?.toDate()
        orderDate?.let {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(it)
            time_orderContextDets.text = formattedDate

        }
        if(focusedOrder.forDelivery){
            delivery_details_orderContextDets.text = deliveryTrue
            if(focusedOrder.orderTimestampConfirmed != null && focusedOrder.orderStatus == 2){
                estimatedDelivery_layout.visibility = View.VISIBLE
                //if timestamp confirmed is not null than orderDate should not be null
                val estimatedDeliveryTime = getEstimatedDeliveryTime(focusedOrder.orderTimestampConfirmed.toDate(), focusedOrder.seller.deliveryTime)
                estimatedDelivery_orderContextDets.text = estimatedDeliveryTime
            }else{
                estimatedDelivery_layout.visibility = View.GONE
            }
        }else{
            delivery_details_orderContextDets.text = deliveryFalse
        }

        distributor_details_orderContextDets.text = focusedOrder.seller.userName
        orderPin_orderDetails.text = focusedOrder.orderPin
        store_name_orderContextDets.text = focusedOrder.customer.businessName
        address_orderContextDets.text = focusedOrder.customer.businessAddress
        total_order_viewDets.text = formatCurrency(focusedOrder.orderTotal)
        seller_phoneNumber.text = focusedOrder.seller.userMobileNumber


        destination_info_button.setOnClickListener {
            val gmmIntentUri =
                Uri.parse("google.navigation:q=$latitude,$longitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        setDialButton(focusedOrder.seller.userMobileNumber)


    }


    private fun getEstimatedDeliveryTime(dateConfirmed: Date, daysToDeliver: Int): String{
        val cal = Calendar.getInstance()
        cal.time = dateConfirmed
        cal.add(Calendar.DATE, daysToDeliver)
        val newDate = cal.time

        return SimpleDateFormat("dd/MM/yyyy").format(newDate)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if(googleMap != null){
            mMap = googleMap
        }
    }

    private fun setDialButton(number: String){
        seller_phoneNumber.setOnClickListener {
            activity?.let {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel: $number"))
                    it.startActivity(intent)
                } catch (e: Exception) {
                    Log.d("ACTION_DIAL", e.toString())
                }

            }
        }
    }


}