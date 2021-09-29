package waybilmobile.company.waybilbuyer.model.orders

import com.google.firebase.firestore.IgnoreExtraProperties
import waybilmobile.company.waybilbuyer.model.products.Product

@IgnoreExtraProperties
data class OrderItem (
    val product: Product = Product(),
    var quantity: Int = 0,
    var subTotal: Double = product.pricePerUnit * quantity
)