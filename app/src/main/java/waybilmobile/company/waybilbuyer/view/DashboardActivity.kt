package waybilmobile.company.waybilbuyer.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_dashboard.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.view.home.HomeFragmentDirections
import waybilmobile.company.waybilbuyer.viewModel.DashboardViewModel

private const val CHANNEL_ID = "ORDER_CHANNEL_ID"


class DashboardActivity : AppCompatActivity() {


    private lateinit var viewModel: DashboardViewModel
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        createNotificationChannel()

        user_name_dashboard.setOnClickListener {
            Navigation.findNavController(this, R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false)
            val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment()
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action)
        }

        basket_dashboard.setOnClickListener{
            Navigation.findNavController(this, R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false)
            val action = HomeFragmentDirections.actionHomeFragmentToBasketFragment()
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action)
        }

        home_button.setOnClickListener {
            Navigation.findNavController(this, R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false)
        }

        viewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)

        observeViewModels()

        viewModel.getUser()
        viewModel.getBasket()





    }

    private fun observeViewModels(){
        viewModel.user.observe(this, Observer { user ->
            user?.let {
                user_name_dashboard.text = it.userName
                viewModel.getUserToken()
            }
        })

        viewModel.signedIn.observe(this, Observer{ signedIn ->
            signedIn?.let {
                if(!it){
                    val intent = Intent(this, LoginActivity::class.java)
                    FirebaseAuth.getInstance().signOut()
                    this.finish()
                    startActivity(intent)
                }
            }
        })

        viewModel.basket.observe(this, Observer{ basket ->
            basket?.let {
                if(!it.isNullOrEmpty()){
                    basket_dashboard.text = it[0].productsOrderList?.size.toString()
                }else{
                    basket_dashboard.text = "0"
                }
            }
        })
    }

//    private fun getBasket(){
//        basketReference.whereEqualTo("orderStatus", 7).addSnapshotListener { value, error ->
//            if (error != null) {
//                Log.d("basketData", "ListenFailed")
//                return@addSnapshotListener
//            }
//
//            if (value != null) {
//                var basket = ArrayList<Order>()
////                val snapshotList = ArrayList<Order>()
//                val documents = value.documents
//                documents.forEach {
//                    val order = it.toObject(Order::class.java)
//                    if (order != null) {
//                        basket.add(order)
//
//                    }
//                }
//                //since there will only be one element in the list we put 0
//                if (!basket.isNullOrEmpty()) {
//                    basket_dashboard.text = basket[0].productsOrderList?.size.toString()
//                } else {
//                    basket_dashboard.text = "0"
//                }
//            }
//        }
//    }


    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = getString(R.string.channel_name)
            val descriptionsText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionsText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d("Notification Channel", "Notification Channel created")
        }
    }

    /**
     * MOVE ALL THIS CODE TO A VIEWMODEL WHEN TESTING IS DONE
     */

//    private fun checkSignedInStatus(user: User){
//        val deviceList = user.connectedDevices
//        for (device in deviceList){
//            if (device.deviceId == userToken && !device.signedIn){
//                val intent = Intent(this, LoginActivity::class.java)
//                FirebaseAuth.getInstance().signOut()
//                this.finish()
//                startActivity(intent)
//            }
//        }
//    }

}
