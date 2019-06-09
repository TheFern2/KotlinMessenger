package tech.kodaman.kotlinmessenger

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        // just a comment for testing pushing to two remotes
        supportActionBar?.title = "Select User"
    }
}
