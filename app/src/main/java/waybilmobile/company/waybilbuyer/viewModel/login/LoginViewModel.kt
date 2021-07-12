package waybilmobile.company.waybilbuyer.viewModel.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import waybilmobile.company.waybilbuyer.model.user.Device
import waybilmobile.company.waybilbuyer.model.user.User

class LoginViewModel: ViewModel() {



    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser: User? = null
    private var userToken: String? = null
    private var deviceTempList = arrayListOf<Device>()

    private var _getUserError = MutableLiveData<Boolean>()
    val getUserError: LiveData<Boolean>
        get() = _getUserError

    private val _userExists = MutableLiveData<Boolean>()
    val userExists: LiveData<Boolean>
        get() = _userExists

    private val _updatingUserData = MutableLiveData<Boolean>()
    val updatingUserData: LiveData<Boolean>
        get() = _updatingUserData

    init {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Token Fetch", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            userToken = task.result

        })
    }



    fun getUser(){
        _updatingUserData.value = true
        Log.d("getUser", "Getting USER")
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        currentUid?.let {
            mFirebaseDatabase.collection("buyers").document(it).get().addOnSuccessListener {documentSnapshot ->
                if(documentSnapshot.exists()){
                    currentUser = documentSnapshot.toObject(User::class.java)
                    deviceTempList = currentUser!!.connectedDevices
                    checkTokenExists()
                }else{
                    _userExists.value = false
                }

            }.addOnFailureListener {
                _getUserError.value = true
            }

        }

    }

    //Check if device has been used before on this account otherwise add it to connectedDevices
    private fun checkTokenExists(){
        Log.d("checkToken", "Checking Token")
        val tokenMatches = deviceTempList.filter { it.deviceId.equals(userToken) }
        if (tokenMatches.isNotEmpty()){
            //If current device exists its status is changed to true in the device list
            for (device in deviceTempList){
                if (device.deviceId == userToken){
                    device.signedIn = true
                    device.lastSignIn = Timestamp.now()
                }
            }
            updateDevices()
        }else{
            deviceTempList.add(Device(userToken, true,  Timestamp.now()))
            updateDevices()
        }
    }

    private fun updateDevices(){
        Log.d("UpdateDevices", "Updating Devices")
        //Note: signedInDevices is created after current device has either been
        //added to the list or changed to signedIn
        val signedInDevices = deviceTempList.filter { it.signedIn }
        val sortedDevicesDate = signedInDevices.sortedBy { it.lastSignIn }

        if(currentUser != null){
            if(currentUser!!.devicesAuthorized == 1){
                for(device in deviceTempList){
                    //ensuring that all devices that are not the current device are labeled as
                    // signedOut
                    if (device.deviceId != userToken){
                        device.signedIn = false
                    }
                }
                Log.d("updateData1Auth", "Executing")
                updateFirebaseData()
            }

            if (currentUser!!.devicesAuthorized > 1){
                Log.d("AuthDev", "${signedInDevices.size} \n $signedInDevices")
                //The list already has the current device and its status which is why we check <=
                if(signedInDevices.size <= currentUser!!.devicesAuthorized){
                    updateFirebaseData()
                }else{
                    for(device in deviceTempList){

                        if(device.deviceId == sortedDevicesDate[0].deviceId){
                            device.signedIn = false
                        }
                    }
                    Log.d("updateData>1Auth", "Executing")
                    updateFirebaseData()
                }
            }


        } else{
            Log.d("userNull", "Current user is NULL")
        }

    }


    private fun updateFirebaseData(){
        val updates = hashMapOf<String, Any>(
            "connectedDevices" to deviceTempList
        )

        mFirebaseDatabase.collection("buyers").document(currentUser!!.id).update(updates)
            .addOnSuccessListener {
                Log.d("updateDevices", "Devices updated successfully")
            }.addOnFailureListener {

            }.addOnCompleteListener {
                _updatingUserData.value = false
            }
    }


    //Token will be generated on app install
    //Batched write with changes to tokens which are signed in or not.
}