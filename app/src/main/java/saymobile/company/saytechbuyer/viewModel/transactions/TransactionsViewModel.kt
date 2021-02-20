package saymobile.company.saytechbuyer.viewModel.transactions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import saymobile.company.saytechbuyer.model.orders.Order

class TransactionsViewModel: ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private val mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val ordersReference = mFirebaseDatabase.collection("buyersOrders")
        .document(userId).collection("orders")
    private val _noOrders = MutableLiveData<Boolean>()
    val noOrders: LiveData<Boolean>
        get() = _noOrders
    private val _listOfOrders = MutableLiveData<List<Order>>()
    val listOfOrders: LiveData<List<Order>>
        get() = _listOfOrders
    private val _ordersLoadError = MutableLiveData<Boolean>()
    val ordersLoadError: LiveData<Boolean>
        get() = _ordersLoadError

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    //This will download all the data from firebase for orders
    //Somewhere i will add listeners for changes in the database

    /**
     * for completed orders make sure to only get orders with today's date
     */
    fun refresh(id: Int) {
        _loading.value = true

        if(id == 1){
            getPendingTransactions()
        }else{
            getCompletedTransactions()
        }
    }

    fun getCompletedTransactions(){
        ordersReference.whereEqualTo("orderStatus", 3)
            .addSnapshotListener { snapshot, error ->
                if (error != null){
//                    Log.d("LiveCompletedTrans", "Listen Failed")
                    _ordersLoadError.value = true
                    _loading.value = false
                }

                if(snapshot != null){
                    val snapshotList = ArrayList<Order>()
                    val document = snapshot.documents

                    document.forEach {
                        val order = it.toObject(Order::class.java)
                        if(order != null){
                            snapshotList.add(order)
                        }
                    }
                    _listOfOrders.value = snapshotList
                    _ordersLoadError.value = false
                    _loading.value = false
                }
            }
    }

    fun getPendingTransactions(){
        ordersReference.whereLessThan("orderStatus", 3)
            .addSnapshotListener { snapshot, error ->
                if (error != null){
//                    Log.d("LiveCompletedTrans", "Listen Failed")
                    _ordersLoadError.value = true
                    _loading.value = false
                }

                if(snapshot != null){
                    val snapshotList = ArrayList<Order>()
                    val document = snapshot.documents

                    document.forEach {
                        val order = it.toObject(Order::class.java)
                        if(order != null){
                            snapshotList.add(order)
                        }
                    }
                    _listOfOrders.value = snapshotList
                    _ordersLoadError.value = false
                    _loading.value = false
                }
            }
    }

}