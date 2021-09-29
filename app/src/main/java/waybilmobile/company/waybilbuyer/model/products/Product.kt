package waybilmobile.company.waybilbuyer.model.products

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Product(
    val productName: String = "",
    //cases, individual item, containers, etc..
    val skuNumber: String = "",
    val pricePerUnit: Double = 0.00,
    val brand: String = "",
    val type: String = "",
    val category: String = "",
    val itemsAvailable: Int = 0,
    val imageRef: String = "",
    val details: String = ""


)