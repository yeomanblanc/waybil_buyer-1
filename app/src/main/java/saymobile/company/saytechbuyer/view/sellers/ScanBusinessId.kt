package saymobile.company.saytechbuyer.view.sellers

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.fragment_scan_business_id.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import saymobile.company.saytechbuyer.R


class ScanBusinessId : Fragment(), ZXingScannerView.ResultHandler {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan_business_id, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_scanBusinessId.setOnClickListener {
            findNavController().navigateUp()
        }

        // Stop use of camera when fragment closes
        Dexter.withContext(activity)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    zxscan.setResultHandler(this@ScanBusinessId)
                    zxscan.startCamera()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(activity, "Permission needs to be granted to scane", Toast.LENGTH_SHORT)
                        .show()
                }

            })
            .check()
    }

    override fun handleResult(rawResult: Result?) {
        scan_instructions.text = rawResult!!.text
    }


}