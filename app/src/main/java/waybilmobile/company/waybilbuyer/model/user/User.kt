package waybilmobile.company.waybilbuyer.model.user

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User (
    val id: String = "",
    val userName: String = "",
//    val userAddress: String = "",
    val userMobileNumber: String = "",
    val businessEmail: String? = "",
    val accountManager: Name? = null,
    val dateJoined: Timestamp? = null,
//    val geoLocation: GeoPoint? = null,
    val connectedDevices: ArrayList<Device> = arrayListOf(),
    val devicesAuthorized: Int = 1


)