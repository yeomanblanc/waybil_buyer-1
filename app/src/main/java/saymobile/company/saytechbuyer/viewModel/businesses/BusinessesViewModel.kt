package saymobile.company.saytechbuyer.viewModel.businesses

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import saymobile.company.saytechbuyer.model.userbusiness.UserBusiness
import java.util.*
import kotlin.collections.ArrayList

class BusinessesViewModel : ViewModel() {

    private var mAuth = FirebaseAuth.getInstance()
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var businessesRef = mFirebaseDatabase.collection("buyersBusinesses")
    private var currentUser = mAuth.currentUser!!.uid

    private val _businessesList = MutableLiveData<List<UserBusiness>>()
    val businessesList: LiveData<List<UserBusiness>>
        get() = _businessesList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _businessListError = MutableLiveData<Boolean>()
    val businessListError: LiveData<Boolean>
        get() = _businessListError

    var filteredList = mutableListOf<UserBusiness>()
    var searchQuery: String? = null

    fun refresh(){
        _loading.value = true
        businessesRef.document(currentUser).collection("businesses").addSnapshotListener { snapshot, error ->
            if(error != null){
                _businessListError.value = true
                _loading.value = false
                return@addSnapshotListener
            }

            if(snapshot != null){
                Log.d("businessList", "Not null: $snapshot")
                val snapShotList = ArrayList<UserBusiness>()
                val values = snapshot.documents
                values.forEach {
                    val business = it.toObject(UserBusiness::class.java)
                    if(business != null){
                        snapShotList.add(business)
                    }
                }

                filteredList = snapShotList
                Log.d("businessList", "$filteredList")
                _businessListError.value = false
                filterBusinesses()
                _loading.value = false
            }else{
                Log.d("businessList", "Null")

                _businessListError.value = false
                _loading.value = false
            }
        }

    }

    fun filterBusinesses(){
        _businessesList.value = filteredList.filter{
            it.businessName.toLowerCase(Locale.ROOT).contains(searchQuery?.toLowerCase()?: "")
        }
    }

}