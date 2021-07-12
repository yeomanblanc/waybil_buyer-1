package waybilmobile.company.waybilbuyer.view.sellers

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_seller_store.*

import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.getFocusedSeller
import waybilmobile.company.waybilbuyer.model.products.Product

import waybilmobile.company.waybilbuyer.viewModel.sellers.SellerStoreViewModel

/**
 * A simple [Fragment] subclass.
 */
class SellerStoreFragment : Fragment(), SellerProductAdapter.OnItemClickListener{

    private lateinit var viewModel: SellerStoreViewModel
    private var productListAdapter = SellerProductAdapter(arrayListOf(), hashMapOf(), this)
    private var subCategoryAdapter = SubCategoryAdapter(arrayListOf())
    private var focusedSeller = getFocusedSeller()
    private var selectedFilterButton: ToggleButton? = null
    private var forDelivery: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * THIS IS IMPORTANT - ALTHOUGH DELIVERY IN SELLER OBJECT MAY BE TRUE THE DELIVERY VARIABLE
         * HERE HAS BEEN CHANGED BASED ON THE BUYER'S LOCATION AND SO WE TAKE FORDELIVERY FROM THE
         * ARGUMENT PASSED FROM THE PREVIOUS FRAGMENT
         */
        arguments?.let {
            forDelivery = SellerStoreFragmentArgs.fromBundle(it).forDelivery
        }

        viewModel = ViewModelProviders.of(this).get(SellerStoreViewModel::class.java)
        viewModel.focusedSeller = focusedSeller
        observeViewModels()


        sellerName_fragment_header.text = focusedSeller?.userName

        close_fragment_sellerStore.setOnClickListener {
            findNavController().navigateUp()
        }

        floatingActionBasket.setOnClickListener {
            val action = SellerStoreFragmentDirections.actionSellerStoreFragmentToBasketFragment()
            Navigation.findNavController(it).navigate(action)
        }

        product_list_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productListAdapter
        }

        recycler_sub_categories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = subCategoryAdapter
        }
        productListAdapter.focusedSeller = focusedSeller?.id
        viewModel.getBasket()

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
                val builder: AlertDialog.Builder? = activity?.let {
                    AlertDialog.Builder(it)
                }

                builder?.let {theBuilder ->
                    theBuilder.setMessage(getString(R.string.basket_with_other_seller_message))
                        ?.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                            findNavController().navigateUp()
                        }?.setPositiveButton(getString(R.string.continuar)) { _, _ ->
                            viewModel.deleteBasket()
                        }?.setCancelable(false)

                    val dialog = theBuilder.create()

                    val basketDetailsMatch = viewModel.matchBasketDetails()

                    if (basketDetailsMatch){
                        val skuQuantPairs = hashMapOf<String, Int>()
                        for (i in currentBasket){
                            skuQuantPairs[i.product.skuNumber] = i.quantity
                        }
                        productListAdapter.updateQuantities(skuQuantPairs)
                        //If the basket seller matches current seller then load products and compare
                        //quantities - otherwise provide option to delete basket
                        viewModel.refresh()
                    }else if(!basketDetailsMatch){
                        dialog.show()
                        activity?.let {theActivity ->
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                                ContextCompat.getColor(
                                    theActivity,
                                    R.color.colorAccent
                                )
                            )
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                                ContextCompat.getColor(
                                    theActivity,
                                    R.color.colorAccent
                                )
                            )
                        }

                    }

                    if (currentBasket.isEmpty()){

                        floatingActionBasket.visibility = View.GONE
                        basketSize_fab.text = "0"
                    }else if(currentBasket.isNotEmpty()){
                        basketSize_fab.text = currentBasket.size.toString()
                        floatingActionBasket.visibility = View.VISIBLE
                    }

                }
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

    override fun onItemClicked(item: Product, addOrRemove: Int) {
        if(viewModel.currentBasket.value.isNullOrEmpty()){
            viewModel.createBasket(item, forDelivery)
        }else{
            viewModel.updateBasket(item, addOrRemove)
        }
    }


}
