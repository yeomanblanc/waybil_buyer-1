package waybilmobile.company.waybilbuyer.model.orders

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import waybilmobile.company.waybilbuyer.model.user.Seller
import waybilmobile.company.waybilbuyer.model.userbusiness.UserBusiness

@IgnoreExtraProperties
data class Order (
    val orderId: String = "",
    val seller: Seller? = null,
    //Add user object in here
    val productsOrderList: ArrayList<OrderItem>? = null,
    val orderStatus: Int? = 0,
    val orderTimestampReceived: Timestamp? = null,
    val orderTimestampConfirmed: Timestamp? = null,
    val orderTimestampDelivered: Timestamp? = null,
    //Total of subtotal of all orderItems
    var orderTotal: Double = 0.0,
    val week: String? = "",
    val customer: UserBusiness = UserBusiness(),
    var forDelivery: Boolean = false,
    var orderPin: String = "",
    val sellerOffersDelivery: Boolean = false,
    val invoiceNumber: String = ""
)