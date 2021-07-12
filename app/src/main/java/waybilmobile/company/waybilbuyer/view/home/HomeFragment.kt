package waybilmobile.company.waybilbuyer.view.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_home.*

import waybilmobile.company.waybilbuyer.R

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        searchDistri_button_home.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToBusinessFragment()
            Navigation.findNavController(it).navigate(action)
        }

        transactions_button_home.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToOrdersFragment()
            Navigation.findNavController(it).navigate(action)
        }

        transactions_button.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToTransactionsFragment()
            Navigation.findNavController(it).navigate(action)
        }
    }

}
