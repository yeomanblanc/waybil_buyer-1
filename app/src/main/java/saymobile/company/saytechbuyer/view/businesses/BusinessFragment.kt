package saymobile.company.saytechbuyer.view.businesses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_business.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.viewModel.businesses.BusinessesViewModel

class BusinessFragment : Fragment() {

    private lateinit var viewModel: BusinessesViewModel
    private var businessListAdapter = BusinessListAdapter(arrayListOf())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_business, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_business.setOnClickListener {
            findNavController().navigateUp()
        }

        add_business_button.setOnClickListener {
            val action = BusinessFragmentDirections.actionBusinessFragmentToAddBusinessFragment()
            Navigation.findNavController(it).navigate(action)
        }

        viewModel = ViewModelProviders.of(this).get(BusinessesViewModel::class.java)

        business_recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = businessListAdapter
        }

        searchView_business.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery = newText
                viewModel.filterBusinesses()
                return true
            }
        })

        observeViewModels()
        viewModel.refresh()
    }

    fun observeViewModels(){
        viewModel.businessesList.observe(viewLifecycleOwner, Observer {businessesList ->
            businessesList?.let {
                if(businessesList.isNotEmpty()){
                    emptyList_message_business.visibility = View.GONE
                    business_recyclerView.visibility = View.VISIBLE
                    businessListAdapter.updateBusinessList(businessesList)
                }else{
                    emptyList_message_business.visibility = View.VISIBLE
                    business_recyclerView.visibility = View.GONE

                }

            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                if(loading){
                    business_progressBar.visibility = View.VISIBLE
                }else{
                    business_progressBar.visibility = View.GONE
                }
            }
        })

        viewModel.businessListError.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                if(error){
                    error_message_business.visibility = View.VISIBLE
                    business_recyclerView.visibility = View.GONE
                    emptyList_message_business.visibility = View.GONE
                }else{
                    //If there is no error the recycler view will become visible because
                    //of the observer above
                    error_message_business.visibility = View.GONE
                }
            }
        })

    }

}