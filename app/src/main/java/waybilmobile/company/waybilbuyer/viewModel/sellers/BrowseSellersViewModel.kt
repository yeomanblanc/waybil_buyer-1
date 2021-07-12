package waybilmobile.company.waybilbuyer.viewModel.sellers

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import waybilmobile.company.waybilbuyer.getFocusedBusiness
import waybilmobile.company.waybilbuyer.model.user.Seller
import java.util.*
import kotlin.collections.ArrayList

class BrowseSellersViewModel : ViewModel() {

    private var functions = FirebaseFunctions.getInstance()
    private var mAuth = FirebaseAuth.getInstance()
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var sellersRef = mFirebaseDatabase.collection("users")

    private val _sellersList = MutableLiveData<List<Seller>>()
    val sellersList: LiveData<List<Seller>>
        get() = _sellersList
    private val _sellersListLoadError = MutableLiveData<Boolean>()
    val sellersListLoadError: LiveData<Boolean>
        get() = _sellersListLoadError


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading


    var filteredList = mutableListOf<Seller>()
    var searchQuery: String? = null


    fun refresh() {

        _loading.value = true
        sellersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
//                Log.d("SellerData", "Customer data listen failed")
                _sellersListLoadError.value = true
                _loading.value = false
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val snapshotList = ArrayList<Seller>()
                val values = snapshot.documents
                values.forEach {
                    val seller = it.toObject(Seller::class.java)
                    if (seller != null) {
                        snapshotList.add(seller)
                    }
                }
                filteredList = snapshotList
                _sellersListLoadError.value = false
                if(snapshotList.isNotEmpty()){
                    parseProviders(snapshotList)
                }else{
                    _loading.value = false
                }
            }
        }

    }


    fun filterSellers() {
        _sellersList.value = filteredList.filter {
            it.userName.toLowerCase(Locale.ROOT).contains(searchQuery?.toLowerCase() ?: "")
        }
    }



    private fun parseProviders(providers: ArrayList<Seller>) {
        //Separating providers that deliver and do not deliver
        val buyerId = mAuth.currentUser?.uid
        val tempList = arrayListOf<Seller>()
        val business = getFocusedBusiness()
        val visible = providers.filter { it.sellerVisibility == true }
        val notVisible = providers.filter { it.sellerVisibility == false }

        business?.let {
            for (seller in visible) {

                val parsedSeller = processDistance(seller)
                tempList.add(parsedSeller)


            }
            for (seller in notVisible){
                buyerId?.let {
                    if(seller.clientPortfolio.contains(buyerId)){
                        val parsedSeller = processDistance(seller)
                        tempList.add(parsedSeller)
                    }
                }

            }
        }

        filteredList = tempList
        filterSellers()
        _sellersListLoadError.value = false
        _loading.value = false

    }

    private fun processDistance(seller: Seller): Seller{
        val arrayRandom = FloatArray(1)

        val business = getFocusedBusiness()
        business?.let {
                Location.distanceBetween(
                    it.businessLocation!!.latitude,
                    it.businessLocation.longitude,
                    seller.geoLocation!!.latitude,
                    seller.geoLocation.longitude, arrayRandom)
                //distance in kilometers from provider to current buyer
                val distance = arrayRandom[0]/1000
                if(distance.toDouble() > seller.operatingRadius!!){
                    seller.deliveryOffered = false
                }
                seller.distance = distance.toDouble()
        }

        return seller

    }



    //    fun getProviders() {
//        _loading.value = true
//        val providerList = arrayListOf<Seller>()
//        Log.d("getNearestProvider", "CALLED NOW")
//        getSellersCloudFunction()
//            .addOnCompleteListener(OnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    val e = task.exception
//                    if (e is FirebaseFunctionsException) {
//                        val code = e.code
//                        val details = e.details
//                        Log.d("Exception", " HERE: : $code -- $details")
//                    }
//
//                    _sellersListLoadError.value = true
//                    _loading.value = false
//
//                    // ...
//                } else {
//                    Log.d("SUCCESS", "SUCCESS!")
//                    val result = task.result
//                    for (i in result) {
//
//                        val doc = i as HashMap<*, *>
//                        val geo = i["geoLocation"] as HashMap<*, *>
//                        val docsGeo = GeoPoint(
//                            geo["_latitude"].toString().toDouble(),
//                            geo["_longitude"].toString().toDouble()
//                        )
//                        val temp = doc["clientPortfolio"].toString().replace("[", "").replace("]", "")
//                        val clientTemp = temp.split(",").map { it.trim() }
//
//
//                        providerList.add(
//                            Seller(doc["userName"].toString(),
//                                doc["id"].toString(),
//                                doc["deliveryOffered"].toString().toBoolean(),
//                                operatingRadius = doc["operatingRadius"].toString().toInt(),
//                                sellerVisibility = doc["sellerVisibility"].toString().toBoolean(),
//                                geoLocation = docsGeo,
//                                clientPortfolio = clientTemp,
//                                deliveryTime = doc["deliveryTime"].toString().toInt(),
//                                deliveryCost = doc["deliveryCost"].toString().toInt()
//                            )
//                        )
//                    }
//
//                    parseProviders(providerList)
//
//                }
//            })
//    }


//    private fun getSellersCloudFunction(): Task<ArrayList<*>> {
//        Log.d("CloudFUNCTION", "CALLED NOW")
//
//        return functions
//            .getHttpsCallable("getProviders")
//            .call()
//            .continueWith { task ->
//                Log.d("RESULT ->", "${task.result.data}")
//                val result = task.result?.data as ArrayList<*>
//                println(result)
//                result
//            }
//    }

}