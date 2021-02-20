package saymobile.company.saytechbuyer.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_dashboard.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.model.orders.Order
import saymobile.company.saytechbuyer.model.user.User
import saymobile.company.saytechbuyer.view.home.HomeFragmentDirections

private const val CHANNEL_ID = "ORDER_CHANNEL_ID"


class DashboardActivity : AppCompatActivity() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private var basketReference = mFirebaseDatabase.collection("buyersOrders")
        .document(userId).collection("orders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        createNotificationChannel()
        Firebase.messaging.isAutoInitEnabled = true

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



        getUser()
        getBasket()



    }

    private fun getBasket(){
        basketReference.whereEqualTo("orderStatus", 7).addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("basketData", "ListenFailed")
                return@addSnapshotListener
            }

            if (value != null) {
                var basket = ArrayList<Order>()
//                val snapshotList = ArrayList<Order>()
                val documents = value.documents
                documents.forEach {
                    val order = it.toObject(Order::class.java)
                    if (order != null) {
                        basket.add(order)

                    }
                }
                //since there will only be one element in the list we put 0
                if (!basket.isNullOrEmpty()) {
                    basket_dashboard.text = basket[0].productsOrderList?.size.toString()
                } else {
                    basket_dashboard.text = "0"
                }
            }
        }
    }

    private fun getUser(){
        var currentUserProf: User
        val userRef = mFirebaseDatabase.collection("buyers").document(userId)
        userRef.addSnapshotListener{value, error ->
            if(error != null){
                Log.d("userData", "ListenFailed")
                return@addSnapshotListener
            }
            if(value != null){
                currentUserProf = value.toObject(User::class.java)!!
                if(currentUserProf.businessName.isNotEmpty()){
                    user_name_dashboard.text = currentUserProf.businessName
                }
            }
        }

    }

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

}
