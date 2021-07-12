package waybilmobile.company.waybilbuyer.view.sellers

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.fragment_scan_business_id.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import waybilmobile.company.waybilbuyer.R


class ScanBusinessId : Fragment(), ZXingScannerView.ResultHandler {

    private var functions = FirebaseFunctions.getInstance()


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
            zxscan.stopCamera()
            zxscan.stopCameraPreview()
            findNavController().navigateUp()
        }

        re_scan_qr.setOnClickListener {
            zxscan.setResultHandler(this@ScanBusinessId)
            zxscan.startCamera()
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
                    //Add Something Later
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(activity, R.string.camera_permission_request, Toast.LENGTH_SHORT)
                        .show()
                }

            })
            .check()
    }

    override fun handleResult(rawResult: Result?) {
        val split = rawResult!!.text.split(":")
        if(split[0] != "waybil"){
            scan_instructions.text = getString(R.string.not_waybil_qrcode_message)
            re_scan_qr.visibility = View.VISIBLE
        }else{
            //use seller id to add buyer id to seller client list
//            scan_instructions.text = split[1]
            requestClientAdd(split[1]).addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    if (e is FirebaseFunctionsException) {
                        val code = e.code
                        val details = e.details
                    }
                    scan_instructions.text = getString(R.string.error)
                    re_scan_qr.visibility = View.VISIBLE

                }else{
                    scan_instructions.text = getString(R.string.success)
                }

            })

        }
        zxscan.stopCameraPreview()
        zxscan.stopCamera()

    }

    //Add to seller list of clients
    private fun requestClientAdd(sellerId: String): Task<String> {
        Log.d("CloudFUNCTION", "CALLED NOW")
        val data = hashMapOf(
            "sellerId" to sellerId
        )

        return functions
            .getHttpsCallable("addCustomerToBuyerCustomerArray")
            .call(data)
            .continueWith { task ->
                Log.d("RESULT ->", "${task.result.data}")
                val result = task.result?.data as String
                result
            }
    }


}