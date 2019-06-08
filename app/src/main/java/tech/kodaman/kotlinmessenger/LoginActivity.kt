package tech.kodaman.kotlinmessenger

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

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

                        Log.d("RegisterActivity", "Sucessfully logged in ${it.result?.user?.uid}")
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                        Log.d("RegisterActivity", "Failed to log in! ${it.message}")
                    }
        }
    }
}
