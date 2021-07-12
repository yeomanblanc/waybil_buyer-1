package waybilmobile.company.waybilbuyer.view.sellers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_product.view.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.formatCurrency
import waybilmobile.company.waybilbuyer.getProgressDrawable
import waybilmobile.company.waybilbuyer.loadImage
import waybilmobile.company.waybilbuyer.model.products.Product

class SellerProductAdapter (
    val productList: ArrayList<Product>,
    var basket: HashMap<String, Int>,
    private val listener: OnItemClickListener) :
    RecyclerView.Adapter<SellerProductAdapter.ProductListViewHolder>() {

    //make a basket live data basket list that updates the online basket every time a change is made
    //set seller id and then  update productList. Maybe make this a function
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

    inner class ProductListViewHolder(var itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener{
        val productPriceStorePage: TextView = itemView.product_price_storePage
        val itemQuantity : TextView = itemView.item_quantity
        val productName : TextView = itemView.product_name_storePage
        val productDetails: TextView = itemView.product_details_storePage
        val soldOutBanner : TextView = itemView.soldOut_banner
        private val addProduct : TextView = itemView.add_product
        private val removeProduct : TextView = itemView.remove_product
        val productImage : ImageView = itemView.product_image_storePage
        val addRemoveToBasket : LinearLayout = itemView.addRemove_toBasket

        init {
            addProduct.setOnClickListener(this)
            removeProduct.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            //Check if its remove or plus
            val position = adapterPosition
            val basketQuantity = basket[productList[position].skuNumber]
                if(v == addProduct){
                if(position != RecyclerView.NO_POSITION){
                    listener.onItemClicked(productList[position], 1)
                }
                    //Cannot remove product if it does no exist in basket
            }else if(v == removeProduct && basketQuantity != null){
                if(position != RecyclerView.NO_POSITION){
                    listener.onItemClicked(productList[position], -1)
                }
            }

        }
    }

    interface OnItemClickListener{
        fun onItemClicked(item : Product, addOrRemove: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_product, parent, false)
        return ProductListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {

        //This refreshes twice when the SellerStoreFragment starts
        //First time with just the product list
            //And with any updates to that product list
        //Second time with the basket quantities
            //And with any updates to basket list
        //Probably not the most efficient way but whatever for now


        //Product Information
        val inventoryItem = productList[position]
        holder.productName.text = inventoryItem.productName
        holder.productDetails.text = inventoryItem.details
        holder.productPriceStorePage.text = formatCurrency(inventoryItem.pricePerUnit)

        //Product image
        val gsReference =
            FirebaseStorage.getInstance().reference.child("/$focusedSeller" +
                    "/inventory//${inventoryItem.skuNumber}/${inventoryItem.imageRef}.jpeg")


        //check to see if product exists in basket in order to get quantity
        val basketQuantity = basket[inventoryItem.skuNumber]
        if(basketQuantity == null){
            holder.itemQuantity.text = "0"
        }else{
            holder.itemQuantity.text = basketQuantity.toString()
        }


        if(inventoryItem.itemsAvailable == 0){
            holder.soldOutBanner.visibility = View.VISIBLE
            holder.addRemoveToBasket.visibility = View.GONE
        }else{
            holder.soldOutBanner.visibility = View.GONE
            holder.addRemoveToBasket.visibility = View.VISIBLE
        }



        holder.productImage.apply {
            clipToOutline = true
        }
        holder.productImage.loadImage(gsReference, getProgressDrawable(holder
            .productImage.context))


    }




}