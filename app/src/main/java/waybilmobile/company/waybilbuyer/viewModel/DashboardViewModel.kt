package waybilmobile.company.waybilbuyer.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_dashboard.*
import waybilmobile.company.waybilbuyer.model.orders.Order
import waybilmobile.company.waybilbuyer.model.user.User

class DashboardViewModel: ViewModel() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    //user is signed in at this point. Should not return null
    private var currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _basket = MutableLiveData<List<Order>>()
    val basket: LiveData<List<Order>>
        get() = _basket

    private val _signedIn = MutableLiveData<Boolean>()
    val signedIn: LiveData<Boolean>
        get() = _signedIn

    private var userToken: String = ""





    fun getUser(){
        mFirebaseDatabase.collection("buyers")
            .document(currentUserId).addSnapshotListener{value, error ->
                if(error != null){
                    Log.d("userData", "ListenFailed")
                    return@addSnapshotListener
                }
                if(value != null){
                    val currentUserProf = value.toObject(User::class.java)!!
                    _user.value = currentUserProf
                }}

    }

    private fun checkSignInStatus(){
        val deviceList = _user.value!!.connectedDevices
        for (device in deviceList){
            if (device.deviceId == userToken && !device.signedIn){
                _signedIn.value = false
            }
        }
    }


    fun getBasket(){
        val basketReference = mFirebaseDatabase.collection("buyersOrders")
            .document(currentUserId).collection("orders")
        basketReference.whereEqualTo("orderStatus", 7).addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("basketData", "ListenFailed")
                return@addSnapshotListener
            }

            if (value != null) {
                var userBasket = ArrayList<Order>()
//                val snapshotList = ArrayList<Order>()
                val documents = value.documents
                documents.forEach {
                    val order = it.toObject(Order::class.java)
                    if (order != null) {
                        userBasket.add(order)

                    }
                }
                _basket.value = userBasket

            }
        }
    }

    fun getUserToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Token Fetch", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            userToken = task.result
            checkSignInStatus()

        })
    }

}