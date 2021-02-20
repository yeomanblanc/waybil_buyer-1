package saymobile.company.saytechbuyer.viewModel.basket

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import saymobile.company.saytechbuyer.model.orders.Order
import saymobile.company.saytechbuyer.model.orders.OrderItem
import saymobile.company.saytechbuyer.model.products.Product

class BasketViewModel: ViewModel() {

    private var mAuth = FirebaseAuth.getInstance()
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var basketRef = mFirebaseDatabase.collection("buyersOrders")
        .document(mAuth.currentUser!!.uid).collection("orders")
    private var purchaseRef = mFirebaseDatabase.collection("orders")

    private var basketListTemp: ArrayList<OrderItem>? = null

    private val _basketList = MutableLiveData<List<OrderItem>>()
    val basketList: LiveData<List<OrderItem>>
        get() = _basketList

    private val _orderData = MutableLiveData<Order>()
    val orderData: LiveData<Order>
        get() = _orderData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _executingOrder = MutableLiveData<Boolean>()
    val executingOrder: LiveData<Boolean>
        get() = _executingOrder

    private val _purchaseComplete = MutableLiveData<Boolean>()
    val purchaseComplete: LiveData<Boolean>
        get() = _purchaseComplete

    private val _noBasket = MutableLiveData<Boolean>()
    val noBasket: LiveData<Boolean>
        get() = _noBasket


    fun refresh() {
        _loading.value = true
        basketRef.whereEqualTo("orderStatus", 7).addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("basketData", "ListenFailed")
                return@addSnapshotListener
            }

            if (value != null) {
                val snapshotList = ArrayList<Order>()
                val values = value.documents
                values.forEach {
                    val order = it.toObject(Order::class.java)
                    if (order != null) {
                        snapshotList.add(order)
                    }
                }
                if (!snapshotList.isNullOrEmpty()) {
                    _orderData.value = snapshotList[0]
                    _basketList.value = snapshotList[0].productsOrderList
                    basketListTemp = snapshotList[0].productsOrderList
                    _loading.value = false
                }else{
                    _loading.value = false
                    _noBasket.value = true
                    _basketList.value = arrayListOf()
                    basketListTemp = null
                    _orderData.value = Order()
                }
            }
        }
    }


    //Update quantity of basket item
    fun updateBasket(orderItem: OrderItem) {
        /**
         * Replace entire OrderItem or just change quantity and price
         * Consider using a LinkedList for operations like this - would have to be implemented
         * in Java
         */
        val itemPosition =
            basketListTemp?.indexOfFirst { it.product.skuNumber == orderItem.product.skuNumber }
        itemPosition?.let {
            basketListTemp?.get(it)?.quantity = orderItem.quantity
            basketListTemp?.get(it)?.subTotal = orderItem.subTotal
        }

        updateDatabase()
        //Check for where item is the same sku, then change quantity
        //Quantity in product class should be var
    }

    fun deleteFromBasket(product: Product) {

        basketListTemp?.removeIf { it.product.skuNumber == product.skuNumber }
        if (basketListTemp?.size == 0) {
            deleteBasket()

        } else {
            updateDatabase()
        }
    }


    private fun updateDatabase() {

        //calculating new order total here
        val orderTotalTemp = basketListTemp?.map { it.subTotal }?.sum()
        val orderTotal = String.format("%.2f", orderTotalTemp).toDouble()

        orderData.value?.invoiceId?.let {
            val updateRef = basketRef.document(it)
            mFirebaseDatabase.runBatch { batch ->
                batch.update(updateRef, "productsOrderList", basketListTemp)
                batch.update(updateRef, "orderTotal", orderTotal)
            }.addOnSuccessListener {
                Log.d("addToBasket", "Order added to basket successfully")
            }.addOnCompleteListener {
                Log.d("addToBasket", "Item successfully added to basket")
            }

        }

    }

    private fun deleteBasket() {
        orderData.value?.invoiceId?.let {
            basketRef.document(it).delete().addOnSuccessListener {
                Log.d("deleteOrder", "order deleted successfully")
            }.addOnCompleteListener {
                /**
                 * Why does _basketList not get triggered unless I refresh
                 */
                refresh()
            }

        }
    }


    /**
     * updating customer order status and sending order to seller database
     * using a hashmap instead of Order object because we need to add a serverTimestamp for when the
     * order is received by the seller. Order already exists on custoemr side so we just upodate
     * the received field with serverTimestamp
     */
    fun executePurchase(){
        val updates = hashMapOf<String, Any>("orderTimestampReceived" to FieldValue.serverTimestamp(),
        "orderStatus" to 1)

        val purchase = createPurchaseHashMap()
        purchase["orderStatus"] = 1
        _executingOrder.value = true

        mFirebaseDatabase.runBatch { batch -> 
            batch.update(basketRef.document(orderData.value!!.invoiceId), updates)
            batch.update(basketRef.document(orderData.value!!.invoiceId), "orderTimestampReceived", FieldValue.serverTimestamp())
            batch.set(purchaseRef.document(orderData.value!!.sellerUid).collection("orders")
                .document(orderData.value!!.invoiceId), purchase)
        }.addOnSuccessListener {
            Log.d("updateOrderStatus", "orderStatus update successful")
        }.addOnCompleteListener {
            _executingOrder.value = false
            _purchaseComplete.value = true
        }

    }

    fun createPurchaseHashMap() : HashMap<String, Any?>{

        val purchase = orderData.value

        return hashMapOf("invoiceId" to purchase?.invoiceId,
        "sellerUid" to purchase?.sellerUid,
        "productsOrderList" to purchase?.productsOrderList,
        "orderStatus" to purchase?.orderStatus,
        "orderTimestampReceived" to FieldValue.serverTimestamp(),
        "orderTimestampConfirmed" to purchase?.orderTimestampConfirmed,
        "orderTimestampDelivered" to purchase?.orderTimestampDelivered,
        "orderTotal" to purchase?.orderTotal,
        "week" to purchase?.week,
        "customer" to purchase?.customer
        )

    }



}