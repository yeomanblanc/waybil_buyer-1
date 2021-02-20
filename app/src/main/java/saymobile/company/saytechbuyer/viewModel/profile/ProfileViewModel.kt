package saymobile.company.saytechbuyer.viewModel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import saymobile.company.saytechbuyer.model.user.User

class ProfileViewModel: ViewModel() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid

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



}