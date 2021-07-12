package waybilmobile.company.waybilbuyer.viewModel.signup

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import waybilmobile.company.waybilbuyer.model.user.User
import java.io.ByteArrayOutputStream

class SignUpViewModel: ViewModel() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val userRef = mFirebaseDatabase.collection("buyers")
    private val transactionsRef = mFirebaseDatabase.collection("buyersTransactions")
    private val orderRef = mFirebaseDatabase.collection("buyersOrders")
    private var _signUpSuccess = MutableLiveData<Boolean>()
    private var _currentUser = MutableLiveData<FirebaseUser>()

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

    fun createUserDatabase(user: User){

        mFirebaseDatabase.runBatch {batch ->
            mAuth.currentUser?.let {
                batch.set(userRef.document(user.id), user)
                batch.set(transactionsRef.document(user.id), hashMapOf("numberOfTransactions" to 0))
                batch.set(orderRef.document(user.id), hashMapOf("numberOfOrders" to 0))
            }

        }.addOnCompleteListener {
            Log.d("Add New User", "SUCCESS")
            if(it.isSuccessful){
                generateQR()
            }else{
                _signUpSuccess.value = false
            }
        }.addOnFailureListener {
//            Log.d("newProfileUpload", "Failed to create profile")
        }
    }

    private fun uploadImage(profileImage: ByteArray, userId: String){
        val imageRef = FirebaseStorage.getInstance().reference.child("/$userId/qr/qr.jpeg")

        val imageTask = imageRef.putBytes(profileImage)
        imageTask.addOnSuccessListener { _signUpSuccess.value = true }
        imageTask.addOnFailureListener { _signUpSuccess.value = false }
        imageTask.addOnCompleteListener { _loading.value = false}
    }


    private fun generateQR(){
        val width = 500
        val height = 500
        val qrData = "saytech:${currentUser.value!!.uid}"
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codewriter = MultiFormatWriter()
        try{
            val bitMatrix = codewriter.encode(qrData, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
        catch (e: WriterException){
            Log.d("Generate QR", "${e.message}")
        }

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        uploadImage(data, currentUser.value!!.uid)
    }


}