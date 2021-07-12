package waybilmobile.company.waybilbuyer.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_sign_up.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.model.user.Device
import waybilmobile.company.waybilbuyer.model.user.Name
import waybilmobile.company.waybilbuyer.model.user.User
import waybilmobile.company.waybilbuyer.viewModel.signup.SignUpViewModel
import java.util.*

private lateinit var currentPhotoPath: String
private const val REQUEST_TAKE_PHOTO = 1

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: SignUpViewModel
//    private var userLocation: GeoPoint? = null
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var pushToken: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        /**
         * REMOVE AND REORGANISE THIS FUNCTIONALITY
         */
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if(!task.isSuccessful){
                Log.w("Token", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            pushToken = task.result
        })

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)

        close_activity_signup.setOnClickListener {
            finish()
        }

        cancel_signup.setOnClickListener {
            finish()
        }

        save_signup.setOnClickListener { registerClicked() }

//        share_location_button.setOnClickListener {
//            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
//                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//            ) {
//                locationSettingsPrompt()
//            } else {
//                startActivity(Intent(this, GeopinActivity::class.java))
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//            }
//        }

        observeViewModel()


    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun observeViewModel() {
        viewModel.loading.observe(this, Observer { loading ->
            loading?.let {
                if (loading) {
                    progressBar_signup.visibility = View.VISIBLE
                } else {
                    progressBar_signup.visibility = View.GONE
                }
            }
        })
        viewModel.signUpSuccess.observe(this, Observer { signUpSuccess ->
            signUpSuccess?.let {
                if (signUpSuccess) {
                    Toast.makeText(this, getString(R.string.signup_successful), Toast.LENGTH_SHORT).show()
                    finish()

                } else {
                    Toast.makeText(this, getString(R.string.signup_failed), Toast.LENGTH_SHORT).show()
                }
            }
        })


    }


    fun registerClicked() {
        val businessName = business_name_signup.text.toString()
        val accountManagerName = account_manager_name.text.toString()
        val accountManagerSurname = account_manager_surname.text.toString()
        val userEmail = email_fillin_signup.text.toString()
        val password = password_fillin_signup.text.toString()
        val phonenumber = phone_fillin_signup.text.toString()
//        val businessAddress = address_fillin_signup.text.toString()
        if (businessName.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_business_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (accountManagerName.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_account_manager_name), Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (accountManagerSurname.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_account_manager_name), Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (phonenumber.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_phone_number), Toast.LENGTH_SHORT).show()
            return
        }

//        if (businessAddress.isEmpty()) {
//            Toast.makeText(applicationContext, getString(R.string.enter_business_address), Toast.LENGTH_SHORT).show()
//            return
//        }

        if (userEmail.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_password), Toast.LENGTH_SHORT).show()
            return
        }
        if (confirm_password_fillin_signup.text.toString().isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_password_confirmation), Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (password_fillin_signup.text.toString() != confirm_password_fillin_signup.text.toString()) {
            Toast.makeText(applicationContext, getString(R.string.passwords_dont_match), Toast.LENGTH_SHORT).show()
            return
        }


//        if (userLocation == null) {
//            Toast.makeText(
//                applicationContext,
//                getString(R.string.need_location),
//                Toast.LENGTH_SHORT
//            ).show()
//            return
//        }

        val devices = arrayListOf<Device>(Device(pushToken, true,
            Timestamp.now()))

        viewModel.currentUser.observe(this, Observer { currentUser ->
            currentUser?.let {
                val user = User(
                    //Add address edittext in signup
                    currentUser.uid, businessName, phonenumber, userEmail,
                    Name(accountManagerName, accountManagerSurname),
                    Timestamp.now(), devices)
                viewModel.createUserDatabase(user)

            }
        })

        viewModel.registerUser(
            email_fillin_signup.text.toString(),
            password_fillin_signup.text.toString()
        )

    }


//    private fun locationSettingsPrompt() {
//        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
//            !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//        ) {
//            val builder = AlertDialog.Builder(this)
//            builder.setMessage(getString(R.string.give_location_permission))
//                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
//                    dialog.dismiss()
//                }.setPositiveButton(getString(R.string.yes)) { _, _ ->
//                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                    startActivity(intent)
//
//                }
//
//            builder.show()
//
//        }
//
//    }

//    override fun onResume() {
//        super.onResume()
//        userLocation = getCurrentLocation()
//        resetTempGeoPoint()
//        if(userLocation == null){
//            geolocation_status_signup.setBackgroundResource(R.drawable.circle_status_pending)
//        }else{
//            geolocation_status_signup.setBackgroundResource(R.drawable.circle_status_confirmed)
//        }
//    }



}

