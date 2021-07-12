package waybilmobile.company.waybilbuyer.model.products

data class Product(
    val productName: String = "",
    //cases, individual item, containers, etc..
    val unitType: String = "",
    val itemsPerUnit: Int = 0,
    val sizePerItem: Int = 0,
    val skuNumber: String = "",
    val pricePerUnit: Double = 0.00,
    val brand: String = "",
    val type: String = "",
    val category: String = "",
    val itemsAvailable: Int = 0,
    val imageRef: String = "",
    val details: String = "$sizePerItem $unitType x $itemsPerUnit"


)