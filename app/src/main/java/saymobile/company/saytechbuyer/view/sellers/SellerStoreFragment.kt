package saymobile.company.saytechbuyer.view.sellers

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_seller_store.*

import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.model.orders.OrderItem
import saymobile.company.saytechbuyer.model.products.Product
import saymobile.company.saytechbuyer.viewModel.sellers.SellerStoreViewModel

/**
 * A simple [Fragment] subclass.
 */
class SellerStoreFragment : Fragment(){

    private lateinit var viewModel: SellerStoreViewModel
    private var productListAdapter = SellerProductAdapter(arrayListOf(), hashMapOf())
    private var subCategoryAdapter = SubCategoryAdapter(arrayListOf())
    private var selectedFilterButton: ToggleButton? = null
    private var sellerStoreId = ""
    private var sellerName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            sellerStoreId = SellerStoreFragmentArgs.fromBundle(it).businessId
            sellerName = SellerStoreFragmentArgs.fromBundle(it).businessName
        }

        viewModel = ViewModelProviders.of(this).get(SellerStoreViewModel::class.java)
        viewModel.focusedSeller = sellerStoreId

        sellerName_fragment_header.text = sellerName

        close_fragment_sellerStore.setOnClickListener {
            findNavController().navigateUp()
        }

        product_list_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productListAdapter
        }

        recycler_sub_categories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = subCategoryAdapter
        }
        productListAdapter.focusedSeller = sellerStoreId
        observeViewModels()
        viewModel.getUser()

        /**
         * Change this later to be associated with focused seller
         */

        //Creating live search ability for user inventory
        search_products.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchQuery = query
                viewModel.filterProducts()
                //put this back in
//                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery = newText
                viewModel.filterProducts()
                return true
            }
        })

        subCategoryAdapter.selectedCategory.observe(viewLifecycleOwner, Observer { selectedCategory ->
            selectedCategory?.let {
                updateListForSelectedCategory(selectedCategory)

            }
        })

        //Observing for changes in subCategory data and notifying recyclerview for update
        viewModel.subCategories.observe(viewLifecycleOwner, Observer { subcategories ->
            subcategories?.let {
                recycler_sub_categories.visibility = if(subcategories.isEmpty()) View.GONE else View.VISIBLE
                subCategoryAdapter.updateSubCategories(subcategories)
            }
        })

        //Setting listeners for filter toggles
        toggle_brand.setOnCheckedAction()
        toggle_category.setOnCheckedAction()
        toggle_type.setOnCheckedAction()

    }






    private fun observeViewModels(){
        viewModel.filteredProducts.observe(viewLifecycleOwner, Observer { inventory ->
            inventory?.let {
                product_list_recycler.visibility = View.VISIBLE
                productListAdapter.updateProductList(inventory)
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                if(loading){
                    progressBar_sellerStore.visibility = View.VISIBLE
                }else{
                    progressBar_sellerStore.visibility = View.GONE
                }
            }
        })

        viewModel.currentBasket.observe(viewLifecycleOwner, Observer { currentBasket ->
            currentBasket?.let {
                // Creating hashmap of basket items and their quantity in order to match those
                // with the seller's inventory
                val skuQuantPairs = hashMapOf<String, Int>()
                for (i in currentBasket){
                    skuQuantPairs[i.product.skuNumber] = i.quantity
                }
                productListAdapter.updateQuantities(skuQuantPairs)
            }
        })
        //listen for new selected item to add to basket
        //first check if basket exists
        productListAdapter.itemToUpdate.observe(viewLifecycleOwner, Observer { currentItemAdd ->
            currentItemAdd?.let {

                val builder = AlertDialog.Builder(activity)
                builder.setMessage(getString(R.string.basket_with_other_seller_message)).setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }.setPositiveButton(getString(R.string.continuar)) { _, _ ->
                    viewModel.deleteBasket()
                }
                val sellerIdMatch = viewModel.matchSellerIdToBasket()
                if(sellerIdMatch){
                    viewModel.checkForBasket(currentItemAdd)
                }else{
                    builder.show()
                }

            }
        })

        productListAdapter.itemToDelete.observe(viewLifecycleOwner, Observer { itemToDelete ->
            itemToDelete?.let {
                viewModel.deleteFromBasket(itemToDelete)
            }
        })
    }

    private fun ToggleButton.setOnCheckedAction(){
        this.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                selectedFilterButton = this
                //update viewmodel data with list of all brands
                viewModel.updateSubCategories(this.id)
            }
            else{
                selectedFilterButton = null
                viewModel.clearSubCategories()
            }

            when(this){
                toggle_brand -> viewModel.selectedBrand.value = null
                toggle_type -> viewModel.selectedType.value = null
                toggle_category -> viewModel.selectedCategory.value = null
            }

            viewModel.filterProducts()
        }
    }

    private fun updateListForSelectedCategory(category: String){
        when(selectedFilterButton){
            toggle_brand -> viewModel.selectedBrand.value = category
            toggle_type -> viewModel.selectedType.value = category
            toggle_category -> viewModel.selectedCategory.value = category
        }
        when(selectedFilterButton){
            toggle_brand -> toggle_brand.text = viewModel.selectedBrand.value
            toggle_type -> toggle_type.text = viewModel.selectedType.value
            toggle_category -> toggle_category.text = viewModel.selectedCategory.value
        }

//        viewModel.clearSubCategories()
        viewModel.filterProducts()
    }





}
