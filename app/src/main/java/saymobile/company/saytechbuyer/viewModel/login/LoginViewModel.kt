package saymobile.company.saytechbuyer.viewModel.login

import android.text.BoringLayout
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import saymobile.company.saytechbuyer.model.user.Device
import saymobile.company.saytechbuyer.model.user.User

class LoginViewModel: ViewModel() {



    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser: User? = null
    private var userToken: String? = null
    private var deviceTempList = arrayListOf<Device>()

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
                currentUser = documentSnapshot.toObject(User::class.java)
                deviceTempList = currentUser!!.connectedDevices
            }.addOnCompleteListener {
                checkTokenExists()
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