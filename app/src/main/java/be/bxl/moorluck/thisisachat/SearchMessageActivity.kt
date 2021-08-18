package be.bxl.moorluck.thisisachat

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.adapters.ChatAdapter
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Grade
import be.bxl.moorluck.thisisachat.models.Message
import be.bxl.moorluck.thisisachat.models.Room
import be.bxl.moorluck.thisisachat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*

class SearchMessageActivity : Activity(), PopupMenu.OnMenuItemClickListener {

    //Firebase

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference: StorageReference

    // View

    private lateinit var etSearch : EditText
    private lateinit var btnSearch : ImageButton
    private lateinit var rvMessage : RecyclerView

    // Adapter

    private lateinit var chatAdapter : ChatAdapter

    // User

    lateinit var user : User

    // Other user

    lateinit var otherUserId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_message)

        //Firebase

        auth = Firebase.auth
        databaseReference = Firebase
            .database(FirebaseConst.URL_DATABASE)
            .reference
        storageReference = Firebase
            .storage(FirebaseConst.URL_STORAGE)
            .reference

        // View

        etSearch = findViewById(R.id.et_search_activity)
        btnSearch = findViewById(R.id.btn_search_activity)
        rvMessage = findViewById(R.id.rv_search_activity)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Get intent

        val messages = intent.getParcelableArrayListExtra<Message>(ChatActivity.MESSAGES) as MutableList<Message>

        // OnClick

        val onImageLongClick : (userId : String, view : View) -> Unit = { userId, view ->
            if (userId != auth.currentUser!!.uid) {
                otherUserId = userId
                showPopUpMenu(view)
            }
        }

        btnSearch.setOnClickListener {
            val messagesFiltered : MutableList<Message> = mutableListOf()
            for (message in messages) {
                if (message.content!!.contains(etSearch.text.toString())) {
                    messagesFiltered.add(message)
                }
            }
            chatAdapter.messages = messagesFiltered
            rvMessage.adapter = chatAdapter
        }

        // Setup rv

        chatAdapter = ChatAdapter(this, auth.currentUser!!.uid, onImageLongClick)
        chatAdapter.messages = messages
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true
        rvMessage.layoutManager = linearLayoutManager
        rvMessage.adapter = chatAdapter


    }

    override fun onResume() {
        super.onResume()

        getUserInfo()
    }

    private fun getUserInfo() {
        if (auth.currentUser != null) {
            databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).get()
                .addOnSuccessListener {
                    user = it.getValue(User::class.java)!!
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while loading user : $it", Toast.LENGTH_LONG).show()
                }
        }
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
                checkIfPrivateRoomExist(auth.currentUser!!.uid, otherUserId)
                true
            }
            else -> {
                false
            }
        }
    }

    private fun checkIfPrivateRoomExist(userId: String, otherUserId: String) {
        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(userId + otherUserId).child(
            FirebaseConst.NAME)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    navigateToPrivateRoom(userId + otherUserId, it.value.toString())
                }

                else {
                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(otherUserId + userId).child(
                        FirebaseConst.NAME)
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
        intent.putExtra(ChatActivity.ROOM_NAME, roomName)
        intent.putExtra(ChatActivity.ROOM_TYPE, FirebaseConst.PRIVATE)
        intent.putExtra(ChatActivity.ROOM_ID, roomId)
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
}