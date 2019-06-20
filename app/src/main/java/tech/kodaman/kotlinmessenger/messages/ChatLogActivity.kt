package tech.kodaman.kotlinmessenger.messages

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import tech.kodaman.kotlinmessenger.R
import tech.kodaman.kotlinmessenger.models.User

const val TAG = "ChatLogActivity"

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null
    var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        //val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        currentUser = LatestMessagesActivity.currentUser!!
        supportActionBar?.title = toUser?.username

        chatLogRecyclerView.adapter = adapter

        keyboardManagement()

        //setupDummyData()
        listenForMessages()

        sendMessageButton.setOnClickListener {
            Log.d(TAG, "Trying to send a message")
            sendMessage()
        }
    }

    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/users-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if(chatMessage != null){
                    Log.d(TAG, chatMessage.message)

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatFromItem(chatMessage.message, currentUser!!))
                    } else{
                        adapter.add(ChatToItem(chatMessage.message, toUser!!))
                    }
                }

                chatLogRecyclerView.scrollToPosition(adapter.itemCount-1)
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }

    private fun sendMessage(){
        val text = messageEditText.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val toUsername = toUser?.username
        val fromUsername = currentUser?.username

        val ref = FirebaseDatabase.getInstance().getReference("/users-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/users-messages/$toId/$fromId").push()

        if(fromId == null) return

        val chatMessage = ChatMessage(ref.key!!,text, fromId, toId!!, System.currentTimeMillis() / 1000)

        ref.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "Saved our chat message ${ref.key}")
                    messageEditText.text.clear()
                    keyboardManagement()
                    //listenForMessages()
                }
        toRef.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "Saved our chat message ${toRef.key}")
                }

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        //val latestMessageTest = FirebaseDatabase.getInstance().getReference("/latest-messages/").child("$fromId/$toId")
        val latestFromChatMessage = LatestChatMessage(fromUsername!!, ref.key!!,text, fromId, toId!!, System.currentTimeMillis() / 1000, false)
        val latestToChatMessage = LatestChatMessage(toUsername!!, ref.key!!,text, fromId, toId!!, System.currentTimeMillis() / 1000, false)

        latestMessageRef.setValue(latestToChatMessage)

        // test
        //val testMessage = TestMessage(text, System.currentTimeMillis() / 1000)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(latestFromChatMessage) // TODO update item here, without creating another latest message
        //latestMessageToRef.child("$toId").child("$fromId").setValue(text)

        //Log.d(TAG, "Latest Message Key $latestMessageKey")
    }

     fun keyboardManagement(){
        // starts chat from bottom
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        chatLogRecyclerView.layoutManager = layoutManager

        // pushes up recycler view when softkeyboard popups up
        chatLogRecyclerView.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                chatLogRecyclerView.postDelayed(Runnable {
                    chatLogRecyclerView.scrollToPosition(
                            chatLogRecyclerView.adapter!!.itemCount -1)
                }, 100)
            }
        }
    }
}

class TestMessage(val message:String, val timestamp:Long){
    constructor(): this("",-1)
}

class ChatMessage(val id:String, val message:String, val fromId:String, val toID:String, val timestamp:Long){
    constructor(): this("","","","",-1)
}

class LatestChatMessage(val toUsername:String, val id:String, val message:String, val fromId:String, val toID:String, val timestamp:Long, val messageSeen:Boolean){
    constructor(): this("","","","","",-1, false)
}

class ChatFromItem(val text:String, val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.fromRowTextView.text = text

        // load image into start
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.fromRowImageView
        Picasso.get().load(uri).into(targetImageView)
    }

}

class ChatToItem(val text:String, val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.ToRowTextView.text = text

        // load image into start
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.ToRowImageView
        Picasso.get().load(uri).into(targetImageView)
    }

}



