package saymobile.company.saytechbuyer.viewModel.transactions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import saymobile.company.saytechbuyer.model.orders.Order

class TransactionDetailViewModel: ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private val mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val orderReference = mFirebaseDatabase.collection("buyersOrders")
        .document(userId).collection("orders")


    private val _focusedOrder = MutableLiveData<Order>()
    val focusedOrder: LiveData<Order>
        get() = _focusedOrder

    //    private val _orderStatus = MutableLiveData<Int>()
//    val orderStatus: LiveData<Int>
//        get() = _orderStatus
//    private val _orderItems = MutableLiveData<List<OrderItem>>()
//    val orderItems: LiveData<List<OrderItem>>
//        get() = _orderItems
    private val _loadError = MutableLiveData<Boolean>()
    val loadError: LiveData<Boolean>
        get() = _loadError
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading


    /**
     * Will need to access the transactions document data
     * Check to see if first order date is null - if it is then add the date of the current order
     * Set most recent order date to date of current order
     */


    fun retrieveOrderItems(invoiceId: String) {
        _loading.value = true

        orderReference.document(invoiceId).addSnapshotListener { snapshot, error ->

            if (error != null) {
                Log.d("OrderDetailData", "Listen Failed")
                _loadError.value = true
            }

            if (snapshot != null) {
                val document = snapshot.toObject(Order::class.java)
                if (document != null) {
                    _focusedOrder.value = document

                }
            }
        }

        _loading.value = false
    }
}