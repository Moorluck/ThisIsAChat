package be.bxl.moorluck.thisisachat

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
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
import java.util.*

class ManageRoomActivity : AppCompatActivity() {

    companion object {
        const val LABEL_COPY = "LABEL_COPY"
    }

    // Firebase

    lateinit var auth : FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var storageReference: StorageReference

    // View

    private lateinit var tvRoomId : TextView

    private lateinit var etName : EditText

    private lateinit var imgCopy : ImageView
    private lateinit var imgRoom : ImageView

    lateinit var btnModify : Button
    lateinit var btnLeaveRoom : Button

    // Room info

    lateinit var room : Room

    // Uri and imgUrl

    private var uri : Uri? = null
    private var imgUrl : String = ""

    // Activity for result

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            uri = it.data?.data
            if (uri != null) {
                val source = ImageDecoder.createSource(contentResolver, uri!!)
                val profileDrawable = ImageDecoder.decodeDrawable(source)
                imgRoom.setImageDrawable(profileDrawable)
            }
        }
    }

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
        btnLeaveRoom = findViewById(R.id.btn_leave_room_manage_profile_activity)

        // Setup the action bar

        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get intent
        val roomId = intent.getStringExtra(ChatActivity.ROOM_ID)
        val roomType = intent.getStringExtra(ChatActivity.ROOM_TYPE)
        if (roomId != null && roomType != null) {
            getRoomInfo(roomId, roomType)
        }

        // OnClick

        imgRoom.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }

        imgCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(LABEL_COPY, roomId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Room ID successfully copied !", Toast.LENGTH_LONG).show()
        }

        btnModify.setOnClickListener {
            uploadImg()
        }

        btnLeaveRoom.setOnClickListener {
            removeRoomFromUser()
        }

    }

    private fun removeRoomFromUser() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.ROOMS)
            .child(room.id!!)
            .removeValue()
            .addOnSuccessListener {
                removeUserFromRoom()
            }
    }

    private fun removeUserFromRoom() {
        databaseReference.child(FirebaseConst.ROOMS).child(room.type!!).child(room.id!!)
            .child(FirebaseConst.USERS).child(auth.currentUser!!.uid)
            .removeValue()
            .addOnSuccessListener {
                val intent = Intent(this, RoomActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
    }

    private fun uploadImg() {
        // Upload image then register the user (can't do the opposite)
        if (uri != null) {
            val fileName = UUID.randomUUID()
            val imageRef = storageReference.child("images/$fileName")

            imageRef.putFile(uri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener {
                        imgUrl = it.toString()
                        updateName()
                    }
                }
        }
        else {
            updateName()
        }
    }

    private fun updateName() {

        val newName = etName.text.toString()

        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.CUSTOM).child(room.id!!).child(FirebaseConst.NAME)
            .setValue(newName)
            .addOnSuccessListener {
                updateImg()
            }

    }

    private fun updateImg() {
        if (imgUrl != "") {
            databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.CUSTOM).child(room.id!!).child(FirebaseConst.PHOTO_REF)
                .setValue(imgUrl)
                .addOnSuccessListener {
                    finish()
                }
        }
        else {
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