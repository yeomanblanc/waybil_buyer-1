package waybilmobile.company.waybilbuyer.viewModel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import waybilmobile.company.waybilbuyer.model.user.User

class ProfileViewModel: ViewModel() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean>
        get() = _deleteSuccess

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user



    fun getUser(){
        var currentUserProf: User
        val userRef = mFirebaseDatabase.collection("buyers").document(userId)
        userRef.addSnapshotListener{value, error ->
            if(error != null){
                Log.d("userData", "ListenFailed")
                return@addSnapshotListener
            }
            if(value != null){
                currentUserProf = value.toObject(User::class.java)!!
                _user.value = currentUserProf

            }
        }

    }

    fun deleteUser(email: String, password: String){
        _loading.value = true
        val credentials = EmailAuthProvider.getCredential(email, password)
        currentUser?.reauthenticate(credentials)?.addOnCompleteListener {
            if (it.isSuccessful){
                currentUser?.delete()!!.addOnCompleteListener {task ->
                    if(task.isSuccessful){
                        Log.d("delete user", "Successfully deleted user")
                        _deleteSuccess.value = true
                    }else{
                        Log.d("delete user", "Failed to delete user")
                        _deleteSuccess.value = false
                    }
                }
                _loading.value = false
            }else{
                Log.d("re-authentication", "Failed to re-authenticate")
                _deleteSuccess.value = false
                _loading.value = false

            }

        }
    }



}