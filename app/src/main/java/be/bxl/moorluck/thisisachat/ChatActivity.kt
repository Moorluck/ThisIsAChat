package be.bxl.moorluck.thisisachat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.adapters.ChatAdapter
import be.bxl.moorluck.thisisachat.api.Url
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Grade
import be.bxl.moorluck.thisisachat.models.Message
import be.bxl.moorluck.thisisachat.models.Room
import be.bxl.moorluck.thisisachat.models.User
import com.bumptech.glide.Glide
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
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    companion object {
        const val ROOM_NAME = "ROOM_NAME"
        const val ROOM_ID = "ROOM_ID"
        const val ROOM_TYPE = "ROOM_TYPE"

        const val MESSAGES = "MESSAGES"
    }

    //Firebase

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference: StorageReference

    // View

    lateinit var clBackground : ConstraintLayout
    lateinit var imgBackground : ImageView

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


    // User

    var userFirebase : FirebaseUser? = null
    var user : User? = null

    // Other user

    lateinit var otherUserId : String

    // Messages

    var messages : MutableList<Message> = mutableListOf()

    // Uri and url
    private var uri : Uri? = null
    private var imgUrl : String =""

    // Activity for result

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            uri = it.data?.data
            if (uri != null) {
                uploadImage()
            }
        }
    }




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
        imgBackground = findViewById(R.id.img_background_chat_activity)

        etMessage = findViewById(R.id.et_message_chat_activity)
        imgSendImage = findViewById(R.id.img_send_image_chat_activity)
        btnSend = findViewById(R.id.btn_send_chat_activity)
        rvMessage = findViewById(R.id.rv_message_chat_activity)

        // Setup the action bar

        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup Rv

        val onImageLongClick : (userId : String, view : View) -> Unit = { userId, view ->
            if (userId != userFirebase!!.uid) {
                otherUserId = userId
                showPopUpMenu(view)
            }
        }

        chatAdapter = ChatAdapter(this, auth.currentUser!!.uid, onImageLongClick)

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true
        rvMessage.layoutManager = linearLayoutManager

        // Get intent and setup title

        roomType = intent.getStringExtra(ROOM_TYPE) ?: ""
        roomName = intent.getStringExtra(ROOM_NAME) ?: ""
        roomId = intent.getStringExtra(ROOM_ID) ?: ""

        // OnClick

        btnSend.setOnClickListener {
            sendMessage()
        }

        imgSendImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }

    }

    override fun onResume() {
        super.onResume()

        setupBackground()
        getUserInfo()
        getMessages()
    }

    private fun showPopUpMenu(view : View) {
        val popup = PopupMenu(this, view)
        popup.setOnMenuItemClickListener(this)
        val menuInflater : MenuInflater = popup.menuInflater
        menuInflater.inflate(R.menu.chat_menu_private_pop_up, popup.menu)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.private_message -> {
                checkIfPrivateRoomExist(userFirebase!!.uid, otherUserId)
                true
            }
            else -> {
                false
            }
        }
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

                if (url != "") {
                    Glide.with(this)
                        .asDrawable()
                        .load(url)
                        .fitCenter()
                        .centerCrop()
                        .into(imgBackground)
                }
            }
    }

    private fun getUserInfo() {
        if (userFirebase != null) {
            databaseReference.child(FirebaseConst.USERS).child(userFirebase!!.uid).get()
                .addOnSuccessListener {
                    user = it.getValue(User::class.java)
                    if (roomType == FirebaseConst.PRIVATE) {
                        getOtherUserName()
                    }
                    else {
                        supportActionBar?.title = roomName
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while loading user : $it", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun getOtherUserName() {
        databaseReference.child(FirebaseConst.USERS).child(roomName.replace(auth.currentUser!!.uid, ""))
            .child(FirebaseConst.PSEUDO)
            .get()
            .addOnSuccessListener {
                supportActionBar?.title = it.value.toString()
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

            databaseReference.child(FirebaseConst.ROOMS).child(roomType).child(roomId).child(FirebaseConst.LAST_MESSAGE)
                .setValue(Message(userFirebase!!.uid, user!!.pseudo, user!!.imgUrl!!, LocalDate.now().toString(), etMessage.text.toString()))

            // Clear ET
            etMessage.text.clear()
        }
    }

    private fun uploadImage() {
        if (uri != null) {
            val fileName = UUID.randomUUID()
            val imageRef = storageReference.child("images/$fileName")

            imageRef.putFile(uri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener {
                        imgUrl = it.toString()
                        sendImage(imgUrl)
                    }
                }
        }
    }

    private fun sendImage(imgUrl: String) {
        databaseReference.child(FirebaseConst.ROOMS).child(roomType).child(roomId).child(FirebaseConst.MESSAGES).push()
            .setValue(Message(userFirebase!!.uid, user!!.pseudo, user!!.imgUrl!!, LocalDate.now().toString(), imgContent = imgUrl))

        databaseReference.child(FirebaseConst.ROOMS).child(roomType).child(roomId).child(FirebaseConst.LAST_MESSAGE)
            .setValue(Message(userFirebase!!.uid, user!!.pseudo, user!!.imgUrl!!, LocalDate.now().toString(), imgContent = imgUrl))
    }

    private fun checkIfPrivateRoomExist(userId: String, otherUserId: String) {
        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(userId + otherUserId).child(FirebaseConst.NAME)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    navigateToPrivateRoom(userId + otherUserId, it.value.toString())
                }

                else {
                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(otherUserId + userId).child(FirebaseConst.NAME)
                        .get()
                        .addOnSuccessListener { room ->
                            if (room.exists()) {
                                navigateToPrivateRoom(otherUserId + userId, room.value.toString())
                            }
                            else {
                                createNewPrivateRoom(userId, otherUserId)
                            }
                        }
                }
            }
    }

    private fun navigateToPrivateRoom(roomId: String, roomName : String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(ROOM_NAME, roomName)
        intent.putExtra(ROOM_TYPE, FirebaseConst.PRIVATE)
        intent.putExtra(ROOM_ID, roomId)
        startActivity(intent)
        finish()
    }

    private fun createNewPrivateRoom(userId: String, otherUserId: String) {
        databaseReference.child(FirebaseConst.USERS).child(otherUserId).child(FirebaseConst.PSEUDO)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val room = Room(
                        type = FirebaseConst.PRIVATE,
                        id = userId + otherUserId,
                        name = userId + otherUserId,
                        users = mapOf(userId to user!!.pseudo!!, otherUserId to it.value.toString()),
                        photoRef = "",
                        grades = mapOf(Grade().name to Grade(users = mapOf(userId to user!!.pseudo!!, otherUserId to it.value.toString())))
                    )

                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(room.id!!).setValue(room)
                        .addOnSuccessListener {
                            addRoomToUser(room, otherUserId)
                        }
                }
            }

    }

    private fun addRoomToUser(room: Room, otherUserId: String) {
            databaseReference.child(FirebaseConst.USERS)
                .child(auth.currentUser!!.uid)
                .child(FirebaseConst.ROOMS)
                .child(UUID.randomUUID().toString())
                .setValue(room.id)
                .addOnSuccessListener {
                    addRoomToOtherUser(room, otherUserId)
                }
    }

    private fun addRoomToOtherUser(room: Room, otherUserId: String) {
        databaseReference.child(FirebaseConst.USERS)
            .child(otherUserId)
            .child(FirebaseConst.ROOMS)
            .child(UUID.randomUUID().toString())
            .setValue(room.id)
            .addOnSuccessListener {
                navigateToPrivateRoom(room.id!!, room.name!!)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (roomType == FirebaseConst.CUSTOM) {
            menuInflater.inflate(R.menu.chat_menu_action_bar_custom, menu)
        }
        else {
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

            R.id.menu_search -> {
                val intent = Intent(this, SearchMessageActivity::class.java)
                intent.putParcelableArrayListExtra(MESSAGES, ArrayList(messages))
                startActivity(intent)
            }

            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}