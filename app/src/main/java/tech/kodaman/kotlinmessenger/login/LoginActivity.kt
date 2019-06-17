package tech.kodaman.kotlinmessenger.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import tech.kodaman.kotlinmessenger.R
import tech.kodaman.kotlinmessenger.messages.LatestMessagesActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton.setOnClickListener {
            Log.d("LoginActivity", "login button pressed!")

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            Log.d("LoginActivity", "email is $email")
            Log.d("LoginActivity", "password is $password")


            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful){
                            //Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }

                        Log.d("LoginActivity", "Successfully logged in ${it.result?.user?.uid}")
                        // Launch messaging activity
                        val intent = Intent(this, LatestMessagesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                        Log.d("LoginActivity", "Failed to log in! ${it.message}")
                    }
        }
    }
}
