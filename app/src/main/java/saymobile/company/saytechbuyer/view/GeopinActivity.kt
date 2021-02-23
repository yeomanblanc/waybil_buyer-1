package saymobile.company.saytechbuyer.view

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.resetTempGeoPoint
import saymobile.company.saytechbuyer.tempGeoPoint

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
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
            updateLocationUI()
            getDeviceLocation()
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
            for (i in 0..4) {

                if (locationPermissionGranted) {
                    val locationResult = fusedLocationProviderClient.lastLocation
                    locationResult.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.result
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
                                userLocation?.let {
                                    tempGeoPoint(it)
                                }
                            }
                        } else {
                            Log.d("Location", "Current location is null. Using defaults.")
                            Log.e("Location", "Exception: %s", task.exception)
                            mMap?.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                            )
                            mMap?.uiSettings?.isMyLocationButtonEnabled = false
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        //Code in case permissions are denied -> end activity
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            updateLocationUI()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }//Else exit activity here
            }
        }
        updateLocationUI()
    }


    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun saveGeopinActivityPrompt() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure this is your location?")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton("Si") { _, _ ->
                //Sends user to device location settings to switch on location services
                finish()

            }

        builder.show()


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