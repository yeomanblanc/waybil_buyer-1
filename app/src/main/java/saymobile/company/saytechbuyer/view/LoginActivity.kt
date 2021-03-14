package saymobile.company.saytechbuyer.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login.*
import saymobile.company.saytechbuyer.R
import saymobile.company.saytechbuyer.viewModel.login.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
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
                getString(R.string.email_notfound_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (password_entry_login.text.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.no_passowrd_error), Toast.LENGTH_SHORT).show()
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

    }





}
