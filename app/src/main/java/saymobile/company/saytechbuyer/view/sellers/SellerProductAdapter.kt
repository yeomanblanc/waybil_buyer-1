package saymobile.company.saytechbuyer.view.sellers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import io.grpc.ProxyDetector
import kotlinx.android.synthetic.main.item_product.view.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.databinding.ItemProductBinding
import saymobile.company.saytechbuyer.formatCurrency
import saymobile.company.saytechbuyer.getProgressDrawable
import saymobile.company.saytechbuyer.loadImage
import saymobile.company.saytechbuyer.model.orders.OrderItem
import saymobile.company.saytechbuyer.model.products.Product

class SellerProductAdapter (val productList: ArrayList<Product>, var basket: HashMap<String, Int>) :
    RecyclerView.Adapter<SellerProductAdapter.ProductListViewHolder>() {

    //make a basket live data basket list that updates the online basket every time a change is made
    //set seller id and then  update productList. Maybe make this a function
    val itemToUpdate = MutableLiveData<OrderItem>()
    val itemToDelete = MutableLiveData<Product>()
    var focusedSeller: String? = ""

    fun updateProductList(newProductList: List<Product>){
        productList.clear()
        productList.addAll(newProductList)
        notifyDataSetChanged()
    }

    fun updateQuantities(newBasket : HashMap<String, Int>){
        basket.clear()
        basket = newBasket
        notifyDataSetChanged()
    }

    class ProductListViewHolder(var view: ItemProductBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemProductBinding>(inflater, R.layout.item_product, parent, false)
        return ProductListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {


        val inventoryItem = productList[position]
        holder.view.productPriceStorePage.text = formatCurrency(inventoryItem.pricePerUnit)
        val gsReference =
            FirebaseStorage.getInstance().reference.child("/$focusedSeller" +
                    "/inventory//${inventoryItem.skuNumber}/${inventoryItem.imageRef}.jpeg")
        //check to see if product exists in basket in order to get quantity
        val basketQuantity = basket[inventoryItem.skuNumber]
        if(basketQuantity == null){
            holder.view.itemQuantity.text = "0"
        }else{
            holder.view.itemQuantity.text = basketQuantity.toString()
        }

        var quantity = holder.view.itemQuantity.text.toString().toInt()
        holder.view.product = productList[position]

        //creates new OrderItem with the products new quantity
        holder.view.addProduct.setOnClickListener {
            quantity += 1
            val newOrderItem = OrderItem(productList[position], quantity)
            itemToUpdate.value = newOrderItem
        }

        holder.view.removeProduct.setOnClickListener {
            if(quantity > 1){
                quantity -= 1
                val newOrderItem = OrderItem(productList[position], quantity)
                itemToUpdate.value = newOrderItem
            } else if(quantity == 1){
//                quantity -= 1
//                holder.view.itemQuantity.text = quantity.toString()
                itemToDelete.value = inventoryItem
            }
        }

        holder.view.productImageStorePage.apply {
            clipToOutline = true
        }
        holder.view.productImageStorePage.loadImage(gsReference, getProgressDrawable(holder
            .view.productImageStorePage.context))

        //load image

    }


}