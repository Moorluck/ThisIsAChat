package be.bxl.moorluck.thisisachat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.adapters.ChatAdapter
import be.bxl.moorluck.thisisachat.fragments.RoomFragment
import be.bxl.moorluck.thisisachat.models.Message
import be.bxl.moorluck.thisisachat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.time.LocalDate

class ChatActivity : AppCompatActivity() {

    //Firebase

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference: StorageReference

    // View

    lateinit var etMessage : EditText
    lateinit var imgSendImage : ImageView
    lateinit var btnSend : Button
    lateinit var rvMessage : RecyclerView

    // Adapter

    lateinit var chatAdapter : ChatAdapter

    // User

    var userFirebase : FirebaseUser? = null
    var user : User? = null

    // Messages

    var messages : MutableList<Message> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Firebase

        auth = Firebase.auth
        userFirebase = auth.currentUser
        databaseReference = Firebase
            .database("https://thisisachat-b0f70-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
        storageReference = Firebase
            .storage("gs://thisisachat-b0f70.appspot.com")
            .reference

        // View

        etMessage = findViewById(R.id.et_message_chat_activity)
        imgSendImage = findViewById(R.id.img_send_image_chat_activity)
        btnSend = findViewById(R.id.btn_send_chat_activity)
        rvMessage = findViewById(R.id.rv_message_chat_activity)

        // Setup Rv

        chatAdapter = ChatAdapter(this, auth.currentUser!!.uid)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true
        rvMessage.layoutManager = linearLayoutManager

        // Setup title

        val roomName = intent.getStringExtra(RoomFragment.ROOM_NAME) ?: ""
        supportActionBar?.title = roomName

        // Get user info

        if (userFirebase != null) {
            databaseReference.child("users").child(userFirebase!!.uid).get()
                .addOnSuccessListener {
                    user = it.getValue(User::class.java)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while loading user : $it", Toast.LENGTH_LONG).show()
                }
        }

        // Get message

        databaseReference.child("rooms").child("place").child(roomName).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages = mutableListOf()
                    snapshot.children.forEach {
                        messages.add(it.getValue(Message::class.java)!!)
                    }
                    chatAdapter.messages = messages
                    rvMessage.adapter = chatAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Firebase", "Failed to connect")
                }

            })

        // OnClick

        btnSend.setOnClickListener {
            if (etMessage.text.isNotEmpty()) {
                databaseReference.child("rooms").child("place").child(roomName).child("messages").push()
                    .setValue(Message(userFirebase!!.uid, user!!.imgUrl!!, LocalDate.now().toString(), etMessage.text.toString()))
            }
        }

    }
}