package saymobile.company.saytechbuyer.model.user

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User (
    val id: String = "",
    val businessName: String = "",
    val userAddress: String = "",
    val userMobileNumber: String = "",
    val businessEmail: String? = "",
    val accountManager: String? = "",
    val dateJoined: Timestamp? = null,
    val geoLocation: GeoPoint? = null


)