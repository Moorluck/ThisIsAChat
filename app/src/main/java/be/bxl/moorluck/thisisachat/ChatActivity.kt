package be.bxl.moorluck.thisisachat

import android.content.Intent
import android.graphics.drawable.Drawable
import android.inputmethodservice.Keyboard
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.adapters.ChatAdapter
import be.bxl.moorluck.thisisachat.api.Url
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.fragments.RoomFragment
import be.bxl.moorluck.thisisachat.models.Message
import be.bxl.moorluck.thisisachat.models.Room
import be.bxl.moorluck.thisisachat.models.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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

    companion object {
        const val ROOM_ID = "ROOM_ID"
        const val ROOM_TYPE = "ROOM_TYPE"
    }

    //Firebase

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference: StorageReference

    // View

    lateinit var clBackground : ConstraintLayout

    lateinit var etMessage : EditText
    lateinit var imgSendImage : ImageView
    lateinit var btnSend : Button
    lateinit var rvMessage : RecyclerView

    // Adapter

    lateinit var chatAdapter : ChatAdapter

    // Room info
    lateinit var roomName : String
    lateinit var roomType : String
    private lateinit var roomId : String

    lateinit var roomChildName : String


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
            .database(FirebaseConst.URL_DATABASE)
            .reference
        storageReference = Firebase
            .storage(FirebaseConst.URL_STORAGE)
            .reference

        // View

        clBackground = findViewById(R.id.cl_background_chat_activity)

        etMessage = findViewById(R.id.et_message_chat_activity)
        imgSendImage = findViewById(R.id.img_send_image_chat_activity)
        btnSend = findViewById(R.id.btn_send_chat_activity)
        rvMessage = findViewById(R.id.rv_message_chat_activity)

        // Setup Rv

        chatAdapter = ChatAdapter(this, auth.currentUser!!.uid)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true
        rvMessage.layoutManager = linearLayoutManager

        // Get intent and setup title

        roomType = intent.getStringExtra(RoomFragment.ROOM_TYPE) ?: ""
        roomName = intent.getStringExtra(RoomFragment.ROOM_NAME) ?: ""
        roomId = intent.getStringExtra(RoomFragment.ROOM_ID) ?: ""
        supportActionBar?.title = roomName

        // Setup the name of the child

        setupBackground()
        getUserInfo()
        getMessages()

        // OnClick

        btnSend.setOnClickListener {
            sendMessage()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (roomType == FirebaseConst.CUSTOM) {
            menuInflater.inflate(R.menu.chat_menu_action_bar, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                val intent = Intent(this, ManageRoomActivity::class.java)
                intent.putExtra(ROOM_ID, roomId)
                intent.putExtra(ROOM_TYPE, roomType)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupBackground() {
        databaseReference.child(FirebaseConst.ROOMS).child(roomType).child(roomId).child(FirebaseConst.PHOTO_REF).get()
            .addOnSuccessListener {
                    val url = if (roomType == FirebaseConst.PLACE) {
                        Url.getPhotoUrl(it.value.toString(), 2000, 2000, Url.getApiKey(this))
                    }
                    else {
                        it.value.toString()
                    }

                    Glide.with(this)
                        .asDrawable()
                        .load(url)
                        .fitCenter()
                        .centerCrop()
                        .into(object : CustomTarget<Drawable>() {

                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                resource.alpha = 70
                                rvMessage.background = resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }

                        })
            }
    }

    private fun getUserInfo() {
        if (userFirebase != null) {
            databaseReference.child(FirebaseConst.USERS).child(userFirebase!!.uid).get()
                .addOnSuccessListener {
                    user = it.getValue(User::class.java)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while loading user : $it", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun getMessages() {
        databaseReference.child(FirebaseConst.ROOMS).child(roomType).child(roomId).child(FirebaseConst.MESSAGES)
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
    }

    private fun sendMessage() {
        if (etMessage.text.isNotEmpty()) {
            databaseReference.child(FirebaseConst.ROOMS).child(roomType).child(roomId).child(FirebaseConst.MESSAGES).push()
                .setValue(Message(userFirebase!!.uid, user!!.pseudo, user!!.imgUrl!!, LocalDate.now().toString(), etMessage.text.toString()))

            // Clear ET
            etMessage.text.clear()
        }
    }
}