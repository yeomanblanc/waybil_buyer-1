package waybilmobile.company.waybilbuyer.model.user

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Device (
    val deviceId: String? = null,
    var signedIn: Boolean = false,
    var lastSignIn: Timestamp? = null
)