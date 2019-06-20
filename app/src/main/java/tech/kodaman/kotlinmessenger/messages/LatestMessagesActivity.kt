package tech.kodaman.kotlinmessenger.messages

import android.content.Intent
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import tech.kodaman.kotlinmessenger.R
import tech.kodaman.kotlinmessenger.login.RegisterActivity
import tech.kodaman.kotlinmessenger.models.User
import java.lang.reflect.Type

class LatestMessagesActivity : AppCompatActivity() {

    //val fromRowMap: MutableMap<String, Int> = mutableMapOf<String, Int>()

    companion object{
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        latestMessagesRecyclerView.adapter = adapter
        listenForLatestMessages()
        fetchCurrentUser()
        VerifyUserIsLoggedIn()
        latestMessagesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.toUser)
            startActivity(intent)
        }
    }

    val latestMessagesMap = HashMap<String, LatestChatMessage>()

    private fun refreshLatestMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object:ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(LatestChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshLatestMessages()
                //adapter.add(LatestMessageRow(chatMessage!!))
                //fetchLatestMessageRow(chatMessage!!.fromId, chatMessage!!.toID)
                //val fromIdIndex = fromRowMap.get(chatMessage!!.fromId)
//                val fromIdIndex = latestMessageRow.row
//                //adapter.notifyItemChanged(0)
//                Log.d(TAG, "${adapter.itemCount}")
//                if(fromIdIndex != null){
//                    Log.d(TAG, "$fromIdIndex")
//                    adapter.removeGroup(fromIdIndex!!)
//                    adapter.add(LatestMessageRow(chatMessage!!))
//                    adapter.notifyItemChanged(fromIdIndex)
                //}
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val chatMessage = p0.getValue(LatestChatMessage::class.java) ?: return
                //val toId = chatMessage.toID
                //val latestMessageRowRef = FirebaseDatabase.getInstance().getReference("/latest-messages-row/$fromId/$toId")

                latestMessagesMap[p0.key!!] = chatMessage
                refreshLatestMessages()
                //adapter.add(LatestMessageRow(chatMessage!!))
//                Log.d(TAG, "onChildAdded")
//                // instead of adding it here we could update it, and refresh rows
//                val itemCount = adapter.itemCount
//
//                if(itemCount > 0){
//                   //fromRowMap.put(chatMessage.fromId, itemCount-1)
//                   val rowId = RowId(itemCount-1)
//                   latestMessageRowRef.setValue(rowId)
//                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }

    val adapter = GroupAdapter<ViewHolder>()
    var latestMessageRow = RowId()

    private fun fetchLatestMessageRow(fromId:String, toId:String){
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages-row/$fromId/$toId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                latestMessageRow = p0.getValue(RowId::class.java)!!
            }

        })
    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
            }

        })
    }


    private fun VerifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            launchRegisterActivity()
        }
    }

    // TODO when clicking on a user, set last message seen to true

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_signout -> {
                FirebaseAuth.getInstance().signOut()
                launchRegisterActivity()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun launchRegisterActivity(){
        val intent = Intent(this, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

class LatestMessageRow(val chatMessage: LatestChatMessage): Item<ViewHolder>(){
    var toUser: User? = null
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        //val toUser = LatestMessagesActivity().fetchUser(chatMessage.toID)

        viewHolder.itemView.userNameTextView.text = chatMessage.toUsername

        // check user
        val partnerId: String
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            partnerId = chatMessage.toID
        } else{
            partnerId = chatMessage.fromId
        }

        // render image
        val ref = FirebaseDatabase.getInstance().getReference("/users/$partnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                toUser = p0.getValue(User::class.java)

                val uri = toUser?.profileImageUrl
                val targetImageView = viewHolder.itemView.latestMessageImageView
                Picasso.get().load(uri).into(targetImageView)

                viewHolder.itemView.latestMessageTextView.text = chatMessage.message
                viewHolder.itemView.latestMessageTextView.setTypeface(null, Typeface.BOLD)

                // have latest message bold if not seen
                if(!chatMessage.messageSeen){
                    //val textView = viewHolder.itemView.latestMessageTextView

                } else{

                }
            }

        })


    }

}

class RowId(val row:Int){
    constructor(): this(0)
}