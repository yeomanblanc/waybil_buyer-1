package saymobile.company.saytechbuyer.viewModel.businesses

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import saymobile.company.saytechbuyer.model.userbusiness.UserBusiness
import saymobile.company.saytechbuyer.view.GeopinActivity

class AddBusinessViewModel : ViewModel() {


    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val businessesRef = mFirebaseDatabase.collection("buyersBusinesses")
    private var currentUser = mAuth.currentUser!!.uid

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean>
        get() = _uploadSuccess

    private val _uploadComplete = MutableLiveData<Boolean>()
    val uploadComplete: LiveData<Boolean>
        get() = _uploadComplete

    private val _uploadFailure = MutableLiveData<Boolean>()
    val uploadFailure: LiveData<Boolean>
        get() = _uploadFailure



    fun submitNewBusiness(businessName: String, businessAddress: String, businessType: String,
        businessLocation: GeoPoint){

        _loading.value = true

        val newDoc = businessesRef.document(currentUser)
            .collection("businesses").document()

        val newBusiness = UserBusiness(currentUser, businessName, newDoc.id, businessAddress,
        businessType, businessLocation)

        newDoc.set(newBusiness).addOnSuccessListener {
            _uploadSuccess
            Log.d("NewBusinessUpload", "Successful")
        }.addOnFailureListener {
            _uploadFailure.value = true
            Log.d("NewBusinessUpload", "Failed")
        }.addOnCompleteListener {
            _uploadComplete.value = true
            _loading.value = false
        }

    }
}