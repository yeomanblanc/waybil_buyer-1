package saymobile.company.saytechbuyer.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import saymobile.company.saytechbuyer.R

class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


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
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }
}
