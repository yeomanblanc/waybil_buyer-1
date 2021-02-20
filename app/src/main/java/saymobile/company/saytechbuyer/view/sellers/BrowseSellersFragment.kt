package saymobile.company.saytechbuyer.view.sellers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_browse_sellers.*

import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.viewModel.sellers.BrowseSellersViewModel


class BrowseSellersFragment : Fragment() {

    private lateinit var viewModel: BrowseSellersViewModel
    private var sellerListAdapter = SellersListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browse_sellers, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_sellers.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProviders.of(this).get(BrowseSellersViewModel::class.java)
        recycler_sellers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sellerListAdapter
        }

        search_sellers.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
//                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery = newText
                viewModel.filterSellers()
                return true
            }
        })

        observeViewModels()
        viewModel.refresh()


    }


    fun observeViewModels(){
        viewModel.sellersList.observe(viewLifecycleOwner, Observer { sellers ->
            sellers?.let {
                recycler_sellers.visibility = View.VISIBLE
                sellerListAdapter.updateSellersList(sellers)
            }
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                if(loading){
                    progressBar_browseSellers.visibility = View.VISIBLE
                }else{
                    progressBar_browseSellers.visibility = View.GONE
                }
            }
        })
    }

}
