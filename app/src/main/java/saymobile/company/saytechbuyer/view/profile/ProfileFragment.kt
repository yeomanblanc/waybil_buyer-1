package saymobile.company.saytechbuyer.view.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*

import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.view.LoginActivity
import saymobile.company.saytechbuyer.viewModel.profile.ProfileViewModel

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {


    private lateinit var viewModel: ProfileViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)


        close_fragment_profile.setOnClickListener {
            findNavController().navigateUp()
        }

        signOut_profilePage.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            FirebaseAuth.getInstance().signOut()
            activity?.finish()
            startActivity(intent)
        }

        observeViewModels()
        viewModel.getUser()


    }


    private fun observeViewModels(){
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                businessName_user_profile.text = user.businessName
                location_user_profile.text = user.userAddress

            }
        })
    }

}
