package waybilmobile.company.waybilbuyer.viewModel.basket

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import waybilmobile.company.waybilbuyer.model.orders.Order
import waybilmobile.company.waybilbuyer.model.orders.OrderItem
import waybilmobile.company.waybilbuyer.model.products.Product
import waybilmobile.company.waybilbuyer.resetFocusedBuyerBusiness
import waybilmobile.company.waybilbuyer.resetFocusedSeller

class BasketViewModel : ViewModel() {

    private var mAuth = FirebaseAuth.getInstance()
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var basketRef = mFirebaseDatabase.collection("buyersOrders")
        .document(mAuth.currentUser!!.uid).collection("orders")

    private val storeRef = mFirebaseDatabase.collection("inventory")

    private var basketListTemp: ArrayList<OrderItem> = arrayListOf()

    private val _basketList = MutableLiveData<List<OrderItem>>()
    val basketList: LiveData<List<OrderItem>>
        get() = _basketList

    private val _orderData = MutableLiveData<Order>()
    val orderData: LiveData<Order>
        get() = _orderData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    var globalProductListener: ListenerRegistration? = null

    private val _executingOrder = MutableLiveData<Boolean>()
    val executingOrder: LiveData<Boolean>
        get() = _executingOrder

    private val _purchaseComplete = MutableLiveData<Boolean>()
    val purchaseComplete: LiveData<Boolean>
        get() = _purchaseComplete

    private val _noBasket = MutableLiveData<Boolean>()
    val noBasket: LiveData<Boolean>
        get() = _noBasket

    private var storeProducts = mutableListOf<Product>()


    fun refresh() {
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
                    basketListTemp = snapshotList[0].productsOrderList!!
                    //If order exists turn on product listener
                    getProductData(_orderData.value!!.seller!!.id)

                    _loading.value = false
                } else {
                    //If order does not exists - turn of product listener
                    globalProductListener?.remove()
                    _loading.value = false
                    _noBasket.value = true
                    _basketList.value = arrayListOf()
                    basketListTemp = arrayListOf()
                    _orderData.value = Order()
                }
            }
        }
    }

    fun getProductData(sellerId: String) {
        _loading.value = true
        globalProductListener = storeRef.document(sellerId).collection("inventory")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    //                    Log.d("LiveDataStore", "ListenFailed")
                    return@addSnapshotListener
                }

                if (value != null) {
                    val snapshotList = ArrayList<Product>()
                    val documents = value.documents
                    documents.forEach {
                        val product = it.toObject(Product::class.java)
                        if (product != null) {
                            snapshotList.add(product)
                        }
                    }

                    storeProducts = snapshotList
                    if (basketListTemp.isNullOrEmpty()) {
                        _loading.value = false
                    } else {
                        _loading.value = false
                        compareQuantities()
                    }

                }
            }
    }


    private fun compareQuantities() {
        //Compare quantities only called if basketListTemp is not null
        val itemsToRemove = arrayListOf<OrderItem>()
        var changes = false
        for (item in basketListTemp) {
            val product = storeProducts.find { it.skuNumber == item.product.skuNumber }
            if (product != null) {
                if (product.itemsAvailable == 0) {
                    itemsToRemove.add(item)
                    changes = true
                } else if (product.itemsAvailable < item.quantity && product.itemsAvailable > 0) {
                    item.quantity = product.itemsAvailable
                    item.subTotal = item.quantity * item.product.pricePerUnit
                    changes = true
                }
            } else {
                itemsToRemove.add(item)
                changes = true
            }
        }

        //Removing items from main basket list
        for (i in itemsToRemove) {
            basketListTemp.removeIf { it.product.skuNumber == i.product.skuNumber }
        }

        //Deleting basket if empty or updating changes
        when {
            basketListTemp.isEmpty() -> {
                println("Calling DELETE sellerVM")
                deleteBasket()
            }
            changes -> {
                println("Calling UPDATE sellerVM")
                updateDatabase()
            }

        }

    }


    //Update quantity of basket item
    fun updateBasket(orderItem: Product, addOrRemove: Int) {
        /**
         * Replace entire OrderItem or just change quantity and price
         * Consider using a LinkedList for operations like this - would have to be implemented
         * in Java
         */
        var updateData = true
        val itemPosition =
            basketListTemp.indexOfFirst { it.product.skuNumber == orderItem.skuNumber }

        basketListTemp[itemPosition].quantity += addOrRemove
        basketListTemp[itemPosition].subTotal = basketListTemp[itemPosition]
            .quantity * orderItem.pricePerUnit
        if(basketListTemp[itemPosition].quantity == 0){
            basketListTemp.removeAt(itemPosition)
            if(basketListTemp.size == 0){
                updateData = false
                deleteBasket()
            }
        }

        if(updateData){
            updateDatabase()
        }

    }

    fun deleteFromBasket(product: Product) {

        basketListTemp.removeIf { it.product.skuNumber == product.skuNumber }
        updateDatabase()

    }


    private fun updateDatabase() {

        //calculating new order total here
        val updates = mutableMapOf<String, Any>()
        var orderTotal = basketListTemp.map { it.subTotal }.sum()
        if(orderData.value?.forDelivery == true){
           orderTotal += orderData.value!!.seller!!.deliveryCost
        }
        updates["productsOrderList"] = basketListTemp
        updates["orderTotal"] = orderTotal

        orderData.value?.orderId?.let {
            val updateRef = basketRef.document(it)
            updateRef.update(updates).addOnSuccessListener {
                Log.d("addToBasket", "Order added to basket successfully")
            }.addOnCompleteListener {
                Log.d("addToBasket", "Item successfully added to basket")
            }

        }

    }

    private fun deleteBasket() {
        orderData.value?.orderId?.let {
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

    fun updateDeliveryStatus(deliver: Boolean){

        var orderTotal = basketListTemp.map { it.subTotal }.sum()
        if(deliver){
            orderTotal += orderData.value!!.seller!!.deliveryCost
        }

        //Updating order total here and not calling updateDatabase as this would result in double
        //the number of reads and writes (2 to 4)
        val updates = mutableMapOf<String, Any>()
        updates["forDelivery"] = deliver
        updates["orderTotal"] = orderTotal

        orderData.value?.orderId?.let {
            val updateRef = basketRef.document(it)
            updateRef.update(updates).addOnCompleteListener {task ->

                Log.d("updateDelivery", "Delivery update successful")
            }

        }
    }



    fun executePurchase() {
        val randFourDigit = (Math.random()*9000) +1000
        val orderPin = String.format("%.0f", randFourDigit)

        val updates = hashMapOf<String, Any>(
            "orderTimestampReceived" to FieldValue.serverTimestamp(),
            "orderStatus" to 1,
            "orderPin" to orderPin
        )

        _executingOrder.value = true

        basketRef.document(orderData.value!!.orderId).update(updates)
            .addOnSuccessListener {
                Log.d("updateOrderStatus", "orderStatus update successful")
            }.addOnCompleteListener {
                _executingOrder.value = false
                _purchaseComplete.value = true
                resetFocusedBuyerBusiness()
                resetFocusedSeller()
            }

    }




}