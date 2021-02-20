package saymobile.company.saytechbuyer.viewModel.sellers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import saymobile.company.saytechbuyer.model.user.User
import java.util.*
import kotlin.collections.ArrayList

class BrowseSellersViewModel: ViewModel() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var sellersRef = mFirebaseDatabase.collection("users")

    private val _sellersList = MutableLiveData<List<User>>()
    val sellersList: LiveData<List<User>>
        get() = _sellersList
    private val _sellersListLoadError = MutableLiveData<Boolean>()
    val sellersListLoadError: LiveData<Boolean>
        get() = _sellersListLoadError


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading


    var filteredList = mutableListOf<User>()
    var searchQuery: String? = null



    fun refresh(){

        _loading.value = true
        sellersRef.addSnapshotListener { snapshot, error ->
            if(error != null){
//                Log.d("SellerData", "Customer data listen failed")
                _sellersListLoadError.value = true
                _loading.value = false
                return@addSnapshotListener
            }

            if(snapshot != null){
                val snapshotList = ArrayList<User>()
                val values = snapshot.documents
                values.forEach {
                    val seller = it.toObject(User::class.java)
                    if(seller != null){
                        snapshotList.add(seller)
                }
                }
                filteredList = snapshotList
                _sellersListLoadError.value = false
                filterSellers()
                _loading.value = false
            }
        }

    }

    fun filterSellers(){
        _sellersList.value = filteredList.filter {
            it.businessName.toLowerCase(Locale.ROOT).contains(searchQuery?.toLowerCase()?: "")
        }
    }
}