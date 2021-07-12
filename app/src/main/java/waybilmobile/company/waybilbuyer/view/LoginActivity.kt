package waybilmobile.company.waybilbuyer.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.viewModel.login.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private lateinit var viewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        observeViewModel()

        mAuth = FirebaseAuth.getInstance()
        login_button.setOnClickListener {
            loginButtonClicked()
        }

        signup_helper.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        no_account_helper.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        forgot_password_helper.setOnClickListener {
            resetPasswordAction()
        }
    }

    override fun onStart() {
        super.onStart()

        //Check if this person is already logged in. If so then it goes straight to the dashboard
        if (mAuth!!.currentUser != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    fun loginButtonClicked() {
        if (email_entry_login.text.isEmpty()) {
            Toast.makeText(
                applicationContext,
                getString(R.string.enter_email_address),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (password_entry_login.text.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.enter_password), Toast.LENGTH_SHORT).show()
            return
        }

        error_login.visibility = View.GONE
        progressBar_login.visibility = View.VISIBLE


        mAuth!!.signInWithEmailAndPassword(
            email_entry_login.text.toString(),
            password_entry_login.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                progressBar_login.visibility = View.GONE
                if (!task.isSuccessful) {
                    error_login.visibility = View.VISIBLE
                } else{
                    /**
                     *
                     */
                    viewModel.getUser()

                }
            }
    }

    fun observeViewModel(){
        viewModel.updatingUserData.observe(this, Observer { updatingUserData ->
            updatingUserData?.let {
                if(!updatingUserData){
                    progressBar_login.visibility = View.GONE
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
        })

        viewModel.userExists.observe(this, Observer { userExists->
            userExists?.let {
                if(!userExists){
                    Toast.makeText(this, R.string.user_does_not_exist, Toast.LENGTH_SHORT).show()
                    mAuth!!.signOut()
                }
            }
        })

    }

    private fun resetPasswordAction(){
        if(email_entry_login.text.toString().isEmpty()){
            Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_LONG).show()
        }else{
            mAuth?.sendPasswordResetEmail(email_entry_login.text.toString())
                ?.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this, R.string.reset_instructions_sent, Toast.LENGTH_LONG).show()
                    }
                }

        }
    }





}
