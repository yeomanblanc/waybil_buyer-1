package waybilmobile.company.waybilbuyer.model.orders

import waybilmobile.company.waybilbuyer.model.products.Product

data class OrderItem (
    val product: Product = Product(),
    var quantity: Int = 0,
    var subTotal: Double = product.pricePerUnit * quantity
)