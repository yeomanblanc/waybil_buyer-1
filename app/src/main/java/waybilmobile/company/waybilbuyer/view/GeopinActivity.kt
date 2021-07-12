package waybilmobile.company.waybilbuyer.view

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_geopin.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.resetTempGeoPoint
import waybilmobile.company.waybilbuyer.tempGeoPoint

private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
private const val DEFAULT_ZOOM = 15

class GeopinActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var userLocation: GeoPoint? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(14.628434, -90.522713)
    private var locationPermissionGranted: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geopin)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_geopoint_activity) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        save_geopoint_activity.setOnClickListener {
            saveGeopinActivityPrompt()
        }

        cancel_geopoint_activity.setOnClickListener {
            cancelGeopinActivity()
        }

        geopoint_shareLocation.setOnClickListener {
            checkLocationPermission()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
            mMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        defaultLocation.latitude,
                        defaultLocation.longitude
                    ), 10.0f
                )
            )
            mMap?.setOnCameraMoveListener {
                val currentCenter = mMap!!.cameraPosition.target
                userLocation = GeoPoint(currentCenter.latitude, currentCenter.longitude)
                userLocation?.let {
                    tempGeoPoint(it)
                }
                Log.d("Camera Movement", "New center is: $currentCenter")
            }
            //Center on user location here
        }
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        Log.d("CurrentUserLocation", "Location: $lastKnownLocation")
                        if (lastKnownLocation != null) {
                            mMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                            userLocation = GeoPoint(
                                lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude
                            )
                            userLocation?.let{
                                tempGeoPoint(it)
                            }
                        }
                    } else {
                        Log.d("Location", "Current location is null. Using defaults.")
                        Log.e("Location", "Exception: %s", task.exception)
                        mMap?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun checkLocationPermission() {
        //Code in case permissions are denied -> end activity
        when{
            ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ->{
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                locationPermissionGranted = true
                getDeviceLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->
                showDialog()

            else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    private fun showDialog(){
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage(getString(R.string.location_rationale_message))
            setTitle(getString(R.string.permission_required))
            setPositiveButton("Ok"){dialog, which ->
                ActivityCompat.requestPermissions(this@GeopinActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            }
        }

        val dialog = builder.create()
        dialog.show()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(this,
                R.color.colorAccent
            )
        )
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
            ContextCompat.getColor(
                this,
                R.color.colorAccent
            )
        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(){
            if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                locationPermissionGranted = true
                getDeviceLocation()
            }
        }

        when (requestCode){
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> innerCheck()
        }

    }

    private fun saveGeopinActivityPrompt() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.is_this_your_location)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton(R.string.yes) { _, _ ->
                finish()

            }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorAccent))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorAccent))


    }

    private fun cancelGeopinActivity(){
        resetTempGeoPoint()
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        resetTempGeoPoint()
        finish()
    }


}