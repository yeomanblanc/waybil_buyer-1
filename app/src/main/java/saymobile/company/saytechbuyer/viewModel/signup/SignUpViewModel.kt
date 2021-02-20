package saymobile.company.saytechbuyer.viewModel.signup

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import saymobile.company.saytechbuyer.model.user.User

class SignUpViewModel: ViewModel() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val userRef = mFirebaseDatabase.collection("buyers")
    private val transactionsRef = mFirebaseDatabase.collection("buyersTransactions")
    private val orderRef = mFirebaseDatabase.collection("buyersOrders")
    private var _signUpSuccess = MutableLiveData<Boolean>()
    private var _currentUser = MutableLiveData<FirebaseUser>()
    private var _finishedUpload = MutableLiveData<Boolean>()
    val finishedUpload: LiveData<Boolean>
        get() = _finishedUpload
    val currentUser: LiveData<FirebaseUser>
        get() = _currentUser
    val signUpSuccess: LiveData<Boolean>
        get() = _signUpSuccess
    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading


    fun registerUser(email: String, password: String){
        _loading.value = true

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
//                    Log.d(ContentValues.TAG, "creatUserWithEmail: Success")
                    _currentUser.value = mAuth.currentUser
                }else{
//                    Log.w(ContentValues.TAG, "createUserWithEmail: Failed", task.exception)
                    _signUpSuccess.value = false
                }
            }

    }

    fun createUserDatabase(user: User, imageUri: Uri){
        userRef.document(user.id).set(user).addOnSuccessListener {
//            Log.d("newProfileUpload", "Successfully create profile")
        }
            .addOnFailureListener {
//                Log.d("newProfileUpload", "Failed to create profile")
            }
            .addOnCompleteListener { uploadImage(imageUri, user.id)}

        mFirebaseDatabase.runBatch {batch ->
            mAuth.currentUser?.let {
                batch.set(userRef.document(it.uid), user)
                batch.set(transactionsRef.document(it.uid), hashMapOf("numberOfTransactions" to 0))
                batch.set(orderRef.document(it.uid), hashMapOf("numberOfOrders" to 0))
            }

        }.addOnCompleteListener {
            uploadImage(imageUri, user.id)
        }.addOnFailureListener {
//            Log.d("newProfileUpload", "Failed to create profile")
        }
    }

    private fun uploadImage(imageUri: Uri, userId: String){
        val imageRef = FirebaseStorage.getInstance().reference.child("/$userId/profileImage.jpeg")
        //just be sure this is always false at the beginning of the process
        val imageTask = imageRef.putFile(imageUri)
        imageTask.addOnSuccessListener { _signUpSuccess.value = true }
        imageTask.addOnFailureListener { _signUpSuccess.value = false }
        imageTask.addOnCompleteListener { _finishedUpload.value = true
            _loading.value = false}
    }
}