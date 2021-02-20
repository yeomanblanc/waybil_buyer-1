package saymobile.company.saytechbuyer.model.orders

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import saymobile.company.saytechbuyer.model.user.User

data class Order (
    val invoiceId: String = "",
    val sellerUid: String = "",
    //Add user object in here
    val productsOrderList: ArrayList<OrderItem>? = null,
    val orderStatus: Int? = 0,
    val orderTimestampReceived: Timestamp? = null,
    val orderTimestampConfirmed: Timestamp? = null,
    val orderTimestampDelivered: Timestamp? = null,
    //Total of subtotal of all orderItems
    var orderTotal: Double = 0.0,
    val week: String? = "",
    val customer: User = User()
)