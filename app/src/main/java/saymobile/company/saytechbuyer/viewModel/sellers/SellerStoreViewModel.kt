package saymobile.company.saytechbuyer.viewModel.sellers

import android.app.AlertDialog
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp

import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.model.orders.Order
import saymobile.company.saytechbuyer.model.orders.OrderItem
import saymobile.company.saytechbuyer.model.products.Product
import saymobile.company.saytechbuyer.model.user.User
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SellerStoreViewModel : ViewModel() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val storeRef = mFirebaseDatabase.collection("inventory")

    //This is null - check why
    private var mAuth = FirebaseAuth.getInstance().currentUser

    private val _currentBasket = MutableLiveData<ArrayList<OrderItem>>()
    val currentBasket: LiveData<ArrayList<OrderItem>>
        get() = _currentBasket

    private var basketListTemp: ArrayList<OrderItem>? = null
    private var orderId: String? = null

    private var storeProducts = mutableListOf<Product>()
    private val _subCategories = MutableLiveData<List<String>>()
    val subCategories: LiveData<List<String>>
        get() = _subCategories

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private var orderData: Order? = null

    private val _deleteBasketQuery = MutableLiveData<Boolean>()
    val deleteBasketQuery: LiveData<Boolean>
        get() = _deleteBasketQuery

    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>>
        get() = _filteredProducts

    val selectedBrand = MutableLiveData<String>()
    val selectedType = MutableLiveData<String>()
    val selectedCategory = MutableLiveData<String>()

    private val inventoryLoadError = MutableLiveData<Boolean>()
    var searchQuery: String? = null
    var focusedSeller: String? = null
    var currentUser: User? = null


    fun refresh() {
        _loading.value = true
        storeRef.document(focusedSeller!!).collection("inventory")
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
                    _loading.value = false
                    filterProducts()
                }
            }
    }

    //Getting user information of buyer
    fun getUser() {
        mAuth?.uid?.let {
            mFirebaseDatabase.collection("buyers").document(it).get()
                .addOnSuccessListener { documentSnapshot ->
                    currentUser = documentSnapshot.toObject(User::class.java)

//                    Log.d("getUser", "User info retrieved successfully $currentUser")
                }.addOnCompleteListener {
                    //Add null check to see if it managed to retrieve the current user
                    getBasket()
                }
        }
    }


    fun getBasket() {
        val basketRef = mFirebaseDatabase.collection("buyersOrders")
            .document(mAuth!!.uid).collection("orders")

        basketRef.whereEqualTo("orderStatus", 7).addSnapshotListener { value, error ->
            if (error != null) {
//                Log.d("basketData", "ListenFailed")
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
                    orderData = basket[0]
                    orderId = basket[0].invoiceId
                    basketListTemp = basket[0].productsOrderList
                    _currentBasket.value = basket[0].productsOrderList
                }else{
                    basketListTemp = null
                    orderData = null
                    //If i set it to null then live data is not checked for a change
                    _currentBasket.value = arrayListOf()
                }

//                Log.d("currentBasket: ", "${_currentBasket.value}")
//                Log.d("basketList: ", "$basketListTemp")
                refresh()
            }
        }
    }

    fun filterProducts() {
        _filteredProducts.value = storeProducts.filter {
            (
                    selectedBrand.value?.equals(it.brand.toLowerCase(Locale.getDefault())) ?: true &&
                            selectedCategory.value?.equals(it.category.toLowerCase(Locale.getDefault())) ?: true &&
                            selectedType.value?.equals(it.type.toLowerCase(Locale.getDefault())) ?: true &&
                            //Needs to be ?: "" handling nullable object
                            it.productName.toLowerCase(Locale.ROOT).contains(
                                searchQuery?.toLowerCase(
                                    Locale.ROOT
                                ) ?: ""
                            ))
        }
    }

    fun updateSubCategories(selectedButton: Int) {
        val currentSubCategoriesList = arrayListOf<String>()
        _filteredProducts.value?.forEach { product ->
            when (selectedButton) {
                R.id.toggle_brand ->
                    if (!currentSubCategoriesList.contains(product.brand.toLowerCase(Locale.getDefault())))
                        currentSubCategoriesList.add(product.brand.toLowerCase(Locale.getDefault()))
                R.id.toggle_type ->
                    if (!currentSubCategoriesList.contains(product.type.toLowerCase(Locale.getDefault())))
                        currentSubCategoriesList.add(product.type.toLowerCase()
                )
                R.id.toggle_category ->
                    if (!currentSubCategoriesList.contains(product.category.toLowerCase(Locale.getDefault())))
                        currentSubCategoriesList.add(product.category.toLowerCase(Locale.getDefault())
                )
            }
        }

        _subCategories.value = currentSubCategoriesList

    }

    fun clearSubCategories() {
        _subCategories.value = listOf()
    }


    /**
     * Check to see if current basket seller id matches the new one
     * If so ask if they want to delete current basket and start a new basket
     * Don't compare if basket does not exist
     */
    fun matchSellerIdToBasket(): Boolean{
        return if(orderData?.sellerUid == null){
            true
        } else orderData?.sellerUid == focusedSeller
    }
    //check for existence of basket - if it does not exist.. create new basket, if it does
    //just update existing basket
    fun checkForBasket(currentItem: OrderItem) {

        //using this for doc id that is then created below
        val basketRef = mFirebaseDatabase.collection("buyersOrders")
            .document(mAuth!!.uid).collection("orders")

        if (currentBasket.value.isNullOrEmpty()) {
//            Log.d("basketNullorEmpty: ", "${currentBasket.value.isNullOrEmpty()}")
            val currentWeek = SimpleDateFormat("W").format(Timestamp.now().toDate())
            val newOrderId = basketRef.document()

            //If currentBasket is empty or null basket is created
            val newOrder = currentUser?.let {
                Order(newOrderId.id, focusedSeller!!, arrayListOf<OrderItem>(), 7,
                    null, null,
                    null, 0.0, currentWeek, it)

            }
            //Selected item is added to the productsOrderList aka Basket
            newOrder?.productsOrderList?.add(currentItem)
            //since this is the first item, orderTotal is equal to the price of this item
            newOrder?.orderTotal = currentItem.subTotal

            //new order is created with containing the newly added product
            if (newOrder != null) {
                createOrder(newOrder)
            }
        } else {
            updateBasket(currentItem)
        }
    }

    //creates order with the initial orderItem added to basket
    private fun createOrder(order: Order) {
        mFirebaseDatabase.collection("buyersOrders").document(mAuth!!.uid)
            .collection("orders").document(order.invoiceId).set(order).addOnSuccessListener {
//                Log.d("createNewBasket", "New basket created successfully")
            }
        orderId = order.invoiceId
    }

    fun updateBasket(orderItem: OrderItem){
        val itemPosition =
            basketListTemp?.indexOfFirst { it.product.skuNumber == orderItem.product.skuNumber }

        itemPosition?.let {
            if (it < 0){
                basketListTemp?.add(orderItem)
            }else{
                basketListTemp?.get(it)?.quantity = orderItem.quantity
                basketListTemp?.get(it)?.subTotal = orderItem.subTotal
            }
        }

        updateDatabase()
    }

    fun updateDatabase(){

        val orderTotal = basketListTemp?.map { it.subTotal }?.sum()

        val basketRef = mFirebaseDatabase.collection("buyersOrders")
            .document(mAuth!!.uid).collection("orders")

        mFirebaseDatabase.runBatch { batch ->
            batch.update(basketRef.document(orderId!!), "productsOrderList", basketListTemp)
            batch.update(basketRef.document(orderId!!), "orderTotal", orderTotal)
        }.addOnSuccessListener {
//            Log.d("addToBasket", "Order added to basket successfully")
        }.addOnCompleteListener {
//            Log.d("addToBasket", "Item successfully added to basket")
        }

    }

    fun deleteFromBasket(product: Product) {

        basketListTemp?.removeIf { it.product.skuNumber == product.skuNumber }
        if (basketListTemp?.size == 0) {
            deleteBasket()
        } else {
            updateDatabase()
        }
    }


    fun deleteBasket(){

        mFirebaseDatabase.collection("buyersOrders").document(currentUser!!.id)
            .collection("orders").document(orderId!!).delete()
            .addOnSuccessListener {
//                Log.d("deleteOrder", "order deleted successfully")
            }
            .addOnCompleteListener {
                getBasket()
            }

    }

}

