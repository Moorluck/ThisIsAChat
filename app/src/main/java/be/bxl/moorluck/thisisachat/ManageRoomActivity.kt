package be.bxl.moorluck.thisisachat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Room
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class ManageRoomActivity : AppCompatActivity() {

    companion object {
        const val LABEL_COPY = "LABEL_COPY"
    }

    //Firebase

    lateinit var auth : FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var storageReference: StorageReference

    // View

    lateinit var tvRoomId : TextView

    lateinit var etName : EditText

    lateinit var imgCopy : ImageView
    lateinit var imgRoom : ImageView

    lateinit var btnModify : Button

    // Room info

    lateinit var room : Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_room)

        // Firebase

        auth = Firebase.auth
        databaseReference = Firebase
            .database(FirebaseConst.URL_DATABASE)
            .reference
        storageReference = Firebase
            .storage(FirebaseConst.URL_STORAGE)
            .reference

        // View

        tvRoomId = findViewById(R.id.tv_room_id_manage_room_activity)

        etName = findViewById(R.id.et_name_manage_room_activity)

        imgCopy = findViewById(R.id.img_copy_id_manage_room_activity)
        imgRoom = findViewById(R.id.img_room_manage_room_activity)

        btnModify = findViewById(R.id.btn_modify_manage_room_activity)

        // Setup the action bar

        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get intent
        val roomId = intent.getStringExtra(ChatActivity.ROOM_ID)
        val roomType = intent.getStringExtra(ChatActivity.ROOM_TYPE)
        if (roomId != null && roomType != null) {
            getRoomInfo(roomId, roomType)
        }

        // OnClick

        imgCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(LABEL_COPY, roomId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Room ID succesfully copied !", Toast.LENGTH_LONG).show()
        }

        btnModify.setOnClickListener {
            updateDB()
        }

    }

    private fun updateDB() {
        val newName = etName.text.toString()

        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.CUSTOM).child(room.id!!).child(FirebaseConst.NAME)
            .setValue(newName)
            .addOnSuccessListener {
                finish()
            }

    }

    private fun getRoomInfo(roomId : String, roomType : String) {
        databaseReference.child(FirebaseConst.ROOMS).child(roomType).child(roomId)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    room = it.getValue(Room::class.java)!!
                    updateUI(room)
                }
            }
    }

    private fun updateUI(room : Room) {
        supportActionBar?.title = room.name

        val text = tvRoomId.text.toString() + " " + room.id
        tvRoomId.text = text

        etName.setText(room.name)

        Glide.with(this)
            .load(room.photoRef)
            .into(imgRoom)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}