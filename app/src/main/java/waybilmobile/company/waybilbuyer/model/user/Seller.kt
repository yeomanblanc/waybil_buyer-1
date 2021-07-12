package waybilmobile.company.waybilbuyer.model.user

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Seller(
    val userName: String = "",
    val id: String = "",
    var deliveryOffered: Boolean? = null,
    val operatingRadius: Int? = null,
    var distance: Double? = null,
    val sellerVisibility: Boolean? = null,
    val geoLocation: GeoPoint? = null,
    val clientPortfolio: List<String> = arrayListOf(),
    val deliveryTime: Int = 0,
    val deliveryCost: Int = 0,
    val profileImageRef: String? = null,
    val userMobileNumber: String = ""

    )