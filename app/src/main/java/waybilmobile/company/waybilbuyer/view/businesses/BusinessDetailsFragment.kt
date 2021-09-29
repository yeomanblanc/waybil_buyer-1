package waybilmobile.company.waybilbuyer.view.businesses

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_business_details.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.getFocusedBusiness


class BusinessDetailsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val businessesRef = mFirebaseDatabase.collection("buyersBusinesses")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_business_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        businessDetails_scrollView.requestDisallowInterceptTouchEvent(true)

        //Child fragment manager is used as we are operating in a fragment activity with a map fragment
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.businessDetails_map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        createOrder_businessDetails.setOnClickListener {
            val action = BusinessDetailsFragmentDirections.actionBusinessDetailsFragmentToBrowseSellersFragment()
            Navigation.findNavController(it).navigate(action)
        }

        close_fragment_businessDetails.setOnClickListener {
            findNavController().navigateUp()
        }

        edit_business_button.setOnClickListener {
            if(delete_business_button.visibility == View.GONE){
                delete_business_button.visibility = View.VISIBLE
            }else{
                delete_business_button.visibility = View.GONE
            }
        }

        delete_business_button.setOnClickListener {
            deleteBusinessQuery()
        }

        setFragmentDetails()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
            setMap()
        }

    }

    private fun setFragmentDetails(){
        val currentBusiness = getFocusedBusiness()
        businessDetails_header.text = currentBusiness?.businessName
        businessName_businessDetails.text = currentBusiness?.businessName
        businessType_businessDetails.text = currentBusiness?.businessType
        businessLocation_businessDetails.text = currentBusiness?.businessAddress
    }

    private fun setMap(){
        val currentBusiness = getFocusedBusiness()
        val longitude = currentBusiness?.businessLocation?.longitude
        val latitude = currentBusiness?.businessLocation?.latitude
        val customerLocation = LatLng(latitude!!, longitude!!)
        mMap.addMarker(MarkerOptions().position(customerLocation).title(currentBusiness.businessName))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 15f))
    }


    private fun deleteBusinessQuery(){
        val builder = AlertDialog.Builder(activity)
        builder.apply {
            setMessage(getString(R.string.delete_business_query))
            setPositiveButton(getString(R.string.yes)){ dialog, _ ->
                deleteBusiness()
                dialog.dismiss()
            }
            setNegativeButton(getString(R.string.cancel)){ dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
        activity?.let {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(it, R.color.colorAccent))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(it, R.color.colorAccent))
        }

    }

    private fun deleteBusiness(){
        val business = getFocusedBusiness()
        business?.let {
            businessesRef.document(it.businessOwnerId).collection("businesses").document(it.businessId).delete().addOnCompleteListener {task->
                if(task.isSuccessful){
                    Toast.makeText(activity, getString(R.string.success), Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }else{
                    Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_LONG).show()
                }
            }

        }
    }



}