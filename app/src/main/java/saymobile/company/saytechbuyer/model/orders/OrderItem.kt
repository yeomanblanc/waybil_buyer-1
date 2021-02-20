package saymobile.company.saytechbuyer.model.orders

import saymobile.company.saytechbuyer.model.products.Product

data class OrderItem (
    val product: Product = Product(),
    var quantity: Int = 0,
    var subTotal: Double = product.pricePerUnit * quantity
)