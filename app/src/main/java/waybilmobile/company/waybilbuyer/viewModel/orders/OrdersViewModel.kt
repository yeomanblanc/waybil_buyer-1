package waybilmobile.company.waybilbuyer.viewModel.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import waybilmobile.company.waybilbuyer.model.orders.Order
import java.util.*
import kotlin.collections.ArrayList

class TransactionsViewModel: ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private val mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val ordersReference = mFirebaseDatabase.collection("buyersOrders")
        .document(userId).collection("orders")
    private val completedOrdersRef = mFirebaseDatabase.collection("buyersTransactions")
        .document(userId)
    private val _noOrders = MutableLiveData<Boolean>()
    val noOrders: LiveData<Boolean>
        get() = _noOrders

    private var currentYearSelected: Int? = null
    private var currentMonthSelected: Int? = null

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

    init {
        currentYearSelected = Calendar.getInstance().get(Calendar.YEAR)
        currentMonthSelected = Calendar.getInstance().get(Calendar.MONTH) + 1
    }
    /**
     * for completed orders make sure to only get orders with today's date
     */
    fun refresh(id: Int) {
        _loading.value = true

        //Just use one as pending and accepted purchases
        if(id == 1){
            getPendingTransactions()
        }else{
            getAcceptedTransactions()
        }
    }

    fun getAcceptedTransactions(){

        ordersReference.whereEqualTo("orderStatus", 2)
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

        ordersReference.whereEqualTo("orderStatus", 1)
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