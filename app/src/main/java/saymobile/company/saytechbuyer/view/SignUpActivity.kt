package saymobile.company.saytechbuyer.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_sign_up.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.getCurrentLocation
import saymobile.company.saytechbuyer.model.user.User
import saymobile.company.saytechbuyer.resetTempGeoPoint
import saymobile.company.saytechbuyer.viewModel.signup.SignUpViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

private lateinit var currentPhotoPath: String
private const val REQUEST_TAKE_PHOTO = 1

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: SignUpViewModel
    private var imageUri: Uri? = null
    private var userLocation: GeoPoint? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)

        close_activity_signup.setOnClickListener {
            finish()
        }

        cancel_signup.setOnClickListener {
            finish()
        }

        save_signup.setOnClickListener { registerClicked() }

        upload_image_button.setOnClickListener { dispatchTakePictureIntent() }

        share_location_button.setOnClickListener {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            ) {
                locationSettingsPrompt()
            } else {
                startActivity(Intent(this, GeopinActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

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
                } else {
                    Toast.makeText(this, getString(R.string.signup_failed), Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.finishedUpload.observe(this, Observer { finishedUpload ->
            finishedUpload?.let {
                finish()
            }
        })
    }


    fun registerClicked() {
        val businessName = business_name_signup.text.toString()
        val accountManager = account_manager_signup.text.toString()
        val userEmail = email_fillin_signup.text.toString()
        val password = password_fillin_signup.text.toString()
        val phonenumber = phone_fillin_signup.text.toString()
        val businessAddress = address_fillin_signup.text.toString()
        if (businessName.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_business_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (accountManager.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_account_manager_name), Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (phonenumber.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_phone_number), Toast.LENGTH_SHORT).show()
            return
        }

        if (businessAddress.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_business_address), Toast.LENGTH_SHORT).show()
            return
        }

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
        if (imageUri == null) {
            Toast.makeText(applicationContext, getString(R.string.please_upload_picture), Toast.LENGTH_SHORT).show()
            return
        }

        if (userLocation == null) {
            Toast.makeText(
                applicationContext,
                getString(R.string.need_location),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        viewModel.currentUser.observe(this, Observer { currentUser ->
            currentUser?.let {
                val user = User(
                    //Add address edittext in signup
                    currentUser.uid, businessName, businessAddress, phonenumber, userEmail, accountManager,
                    Timestamp.now(), userLocation)
                viewModel.createUserDatabase(user, imageUri!!)

            }
        })

        viewModel.registerUser(
            email_fillin_signup.text.toString(),
            password_fillin_signup.text.toString()
        )

    }

    fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(applicationContext.packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        applicationContext,
                        "saymobile.company.saytechbuyer.fileprovider",
                        it
                    )
                    imageUri = photoURI
//                    testingPath = photoFile
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            val imageTaken = BitmapFactory.decodeFile(currentPhotoPath)
            pic_preview.setImageBitmap(imageTaken)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name and add unique file name. Will change this to and ID associated
        // with worker or product in the saytech app
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? =
            applicationContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }



    private fun locationSettingsPrompt() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.give_location_permission))
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)

                }

            builder.show()

        }

    }

    override fun onResume() {
        super.onResume()
        userLocation = getCurrentLocation()
        resetTempGeoPoint()
        if(userLocation == null){
            geolocation_status_signup.setBackgroundResource(R.drawable.circle_status_pending)
        }else{
            geolocation_status_signup.setBackgroundResource(R.drawable.circle_status_confirmed)
        }
    }



}

