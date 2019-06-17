package tech.kodaman.kotlinmessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row.view.*
import tech.kodaman.kotlinmessenger.R
import tech.kodaman.kotlinmessenger.models.User


class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        // just a comment for testing pushing to two remotes
        // one more useless comment for testing git
        supportActionBar?.title = "Select User"

//        val adapter = GroupAdapter<ViewHolder>() // adapter using groupie
//
//        // adapter needs objects to show
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        newMessageRecyclerView.adapter = adapter

        fetchUsers()
    }

    companion object{
        val USER_KEY = "SOME_STRING"
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach{
                    Log.d("NewMessageActivity Chan", it.toString())
                    val user = it.getValue(User::class.java)
                    if(user != null){
                        adapter.add(UserItem(user))
                    }
                    adapter.setOnItemClickListener { item, view ->
                        val userItem = item as UserItem
                        val intent = Intent(view.context, ChatLogActivity::class.java)
                        //intent.putExtra(USER_KEY, userItem.user.username)
                        intent.putExtra(USER_KEY, userItem.user)
                        startActivity(intent)
                        finish()
                    }
                }
                newMessageRecyclerView.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("NewMessageActivity Canc", p0.message)
            }
        })
    }
}

class UserItem(val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.user_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.usernameTextView.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.usernameCircleView)

        // load some arbitrary image is profileurl string is null
        if(user.profileImageUrl.equals("null")){
            Log.d("NewMessageActivity", "image is null for ${user.username}")
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/kotlinmessenger-a8d7b.appspot.com/o/images%2Fdefault-avatar.png?alt=media&token=8b630c60-824e-4a55-883e-362177b0cd6e").into(viewHolder.itemView.usernameCircleView)
        }
    }

}
