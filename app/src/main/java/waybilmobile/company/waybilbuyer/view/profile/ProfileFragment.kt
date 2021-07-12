package waybilmobile.company.waybilbuyer.view.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*

import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.view.LoginActivity
import waybilmobile.company.waybilbuyer.viewModel.profile.ProfileViewModel

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


        scanButton_user_profile.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToScanBusinessId()
            Navigation.findNavController(it).navigate(action)
        }

        moreOptions_button_profile.setOnClickListener {
            if (signOut_profilePage.visibility == View.GONE){
                signOut_profilePage.visibility = View.VISIBLE
                email_layout_settings.visibility = View.VISIBLE
                password_layout_settings.visibility = View.VISIBLE
                delete_settingsPage.visibility = View.VISIBLE
            }else{
                signOut_profilePage.visibility = View.GONE
                email_layout_settings.visibility = View.GONE
                password_layout_settings.visibility = View.GONE
                delete_settingsPage.visibility = View.GONE
            }
        }

        delete_settingsPage.setOnClickListener {
            if(email_edit_settings.text.isEmpty() || password_edit_settings.text.isEmpty()){
                Toast.makeText(activity, R.string.complete_fields_suggestion, Toast.LENGTH_LONG).show()
            }else{
                viewModel.deleteUser(email_edit_settings.text.toString(),
                    password_edit_settings.text.toString())
            }
        }


        observeViewModels()
        viewModel.getUser()


    }


    private fun observeViewModels(){
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                businessName_profile.text = user.userName
//                location_user_profile.text = user.userAddress

            }
        })

        viewModel.deleteSuccess.observe(viewLifecycleOwner, Observer { deleteSuccess ->
            deleteSuccess?.let {
                if(deleteSuccess){
                    val intent = Intent(activity, LoginActivity::class.java)
                    activity?.finish()
                    startActivity(intent)
                }else{
                    Toast.makeText(activity, R.string.delete_user_failed, Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                progressBar_settingsPage.visibility = if(loading) View.VISIBLE else View.GONE
            }
        })
    }

}
