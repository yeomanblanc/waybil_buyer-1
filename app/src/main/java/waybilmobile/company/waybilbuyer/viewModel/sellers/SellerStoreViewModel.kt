package waybilmobile.company.waybilbuyer.viewModel.sellers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.getFocusedBusiness
import waybilmobile.company.waybilbuyer.model.orders.Order
import waybilmobile.company.waybilbuyer.model.orders.OrderItem
import waybilmobile.company.waybilbuyer.model.products.Product
import waybilmobile.company.waybilbuyer.model.user.Seller
import waybilmobile.company.waybilbuyer.model.user.User
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

    private var basketListTemp: ArrayList<OrderItem> = arrayListOf()
    private var orderId: String? = null

    private var storeProducts = mutableListOf<Product>()
    private val _subCategories = MutableLiveData<List<String>>()
    val subCategories: LiveData<List<String>>
        get() = _subCategories

    private val _basketDetailsMatch = MutableLiveData<Boolean>()
    val basketDetailsMatch: LiveData<Boolean>
        get() = _basketDetailsMatch

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
    var focusedSeller: Seller? = null
    var currentUser: User? = null


    fun refresh() {
        _loading.value = true
        storeRef.document(focusedSeller!!.id).collection("inventory")
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

                    if(!basketListTemp.isNullOrEmpty()){
                        compareInventoryToBasketQuantity()
                    }
                    filterProducts()

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
                var snapshotList = ArrayList<Order>()
//                val snapshotList = ArrayList<Order>()
                val documents = value.documents
                documents.forEach {
                    val order = it.toObject(Order::class.java)
                    if (order != null) {
                        snapshotList.add(order)

                    }
                }
                //since there will only be one element in the list we put 0
                if (!snapshotList.isNullOrEmpty()) {
                    orderData = snapshotList[0]
                    orderId = snapshotList[0].orderId
                    _currentBasket.value = snapshotList[0].productsOrderList
                    basketListTemp = snapshotList[0].productsOrderList!!
                }else{
                    basketListTemp = arrayListOf()
                    orderData = null
                    _currentBasket.value = arrayListOf()
                }

//                Log.d("currentBasket: ", "${_currentBasket.value}")
//                Log.d("basketList: ", "$basketListTemp")
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





    private fun compareInventoryToBasketQuantity(){
        val itemsToRemove = arrayListOf<OrderItem>()
        var changes = false
        println("BASKET: $basketListTemp")
        for (item in basketListTemp) {
            val product = storeProducts.find { it.skuNumber == item.product.skuNumber }
            if (product != null) {
                if (product.itemsAvailable == 0) {
                    itemsToRemove.add(item)
                    println("CHANGE 1")
                    changes = true
                } else if (product.itemsAvailable < item.quantity && product.itemsAvailable > 0) {
                    println("CHANGE 2")
                    item.quantity = product.itemsAvailable
                    item.subTotal = item.quantity * item.product.pricePerUnit
                    changes = true
                }
            } else {
                println("CHANGE 3")
                itemsToRemove.add(item)
                changes = true
            }
        }

        //Removing items from main basket list
        for (i in itemsToRemove) {
            basketListTemp.removeIf { it.product.skuNumber == i.product.skuNumber }
        }
//        _currentBasket.value = basketListTemp
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
     * also matching focused business ids
     */
    fun matchBasketDetails(): Boolean{
        return if(orderData?.seller?.id == null){
            true
        } else !(orderData?.seller?.id != focusedSeller?.id || orderData?.customer?.businessId !=
                getFocusedBusiness()?.businessId)
    }




    //check for existence of basket - if it does not exist.. create new basket, if it does
    //just update existing basket
    fun createBasket(currentProduct: Product, deliveryOffered: Boolean) {

        if (currentBasket.value.isNullOrEmpty()) {
            //using this for doc id that is then created below
            val basketRef = mFirebaseDatabase.collection("buyersOrders")
                .document(mAuth!!.uid).collection("orders")
            val focusedBusiness = getFocusedBusiness()
            val currentWeek = SimpleDateFormat("W").format(Timestamp.now().toDate())
            val newOrderId = basketRef.document()

            //If currentBasket is empty or null basket is created
            val newOrder = Order(newOrderId.id, focusedSeller, arrayListOf<OrderItem>(), 7,
                    null, null,
                    null, 0.0, currentWeek, focusedBusiness!!,
                deliveryOffered, sellerOffersDelivery = deliveryOffered,
                )

            val currentItem = OrderItem(currentProduct, 1)
            //Selected item is added to the productsOrderList aka Basket
            newOrder.productsOrderList?.add(currentItem)
            //since this is the first item, orderTotal is equal to the price of this item
            if(deliveryOffered){
                newOrder.orderTotal = currentProduct.pricePerUnit + newOrder.seller!!.deliveryCost
            }else{
                newOrder.orderTotal = currentProduct.pricePerUnit
            }

            //new order is created with containing the newly added product
            createOrder(newOrder)
        }
    }




    //creates order with the initial orderItem added to basket
    private fun createOrder(order: Order) {
        mFirebaseDatabase.collection("buyersOrders").document(mAuth!!.uid)
            .collection("orders").document(order.orderId).set(order).addOnSuccessListener {
//                Log.d("createNewBasket", "New basket created successfully")
            }
//        orderId = order.invoiceId
    }





    fun updateBasket(orderItem: Product, addOrRemove: Int){
        //getting position of item that had a change - if it does not exist then add to basket
        var updateData = true
        val itemPosition =
            basketListTemp.indexOfFirst { it.product.skuNumber == orderItem.skuNumber }


            if (itemPosition < 0){
                val newOrderItem = OrderItem(orderItem, 1)
                basketListTemp.add(newOrderItem)
            }else{
                basketListTemp[itemPosition].quantity += addOrRemove
                basketListTemp[itemPosition].subTotal = basketListTemp[itemPosition].quantity * orderItem.pricePerUnit

                if(basketListTemp[itemPosition].quantity == 0){
                    basketListTemp.removeAt(itemPosition)
                    if(basketListTemp.size == 0){
                        updateData = false
                        deleteBasket()
                    }
                }

            }
        //If the basket was deleted there is nothing to update
        if(updateData){
            updateDatabase()

        }
    }






    private fun updateDatabase(){

        println("UPDATE -> CALLED IN SELLER STOREVM")
        var orderTotal = basketListTemp.map { it.subTotal }.sum()
        //If delivery is selected that totalCost includes deliveryCost
        if(orderData!!.forDelivery){
            orderTotal += orderData!!.seller!!.deliveryCost
        }

        val basketRef = mFirebaseDatabase.collection("buyersOrders")
            .document(mAuth!!.uid).collection("orders")
        val updates = hashMapOf<String, Any?>("productsOrderList" to basketListTemp,
            "orderTotal" to orderTotal
        )

        basketRef.document(orderId!!).update(updates).addOnSuccessListener {
//            Log.d("addToBasket", "Order added to basket successfully")
        }.addOnCompleteListener {
//            Log.d("addToBasket", "Item successfully added to basket")
        }

    }




//    fun deleteFromBasket(product: Product) {
//
//        basketListTemp.removeIf { it.product.skuNumber == product.skuNumber }
//        if (basketListTemp.size == 0) {
//            deleteBasket()
//        } else {
//            updateDatabase()
//        }
//    }


    fun deleteBasket(){

        mFirebaseDatabase.collection("buyersOrders").document(mAuth!!.uid)
            .collection("orders").document(orderId!!).delete()
            .addOnSuccessListener {
//                Log.d("deleteOrder", "order deleted successfully")
            }
            .addOnCompleteListener {
                refresh()
            }

    }

}

