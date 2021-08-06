package be.bxl.moorluck.thisisachat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class ManageRoomActivity : AppCompatActivity() {

    //Firebase

    lateinit var auth : FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var storageReference: StorageReference

    // View

    lateinit var tvRoomId : TextView

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

        // Setup the action bar

        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get intent
        val roomId = intent.getStringExtra(ChatActivity.ROOM_ID)
        val roomType = intent.getStringExtra(ChatActivity.ROOM_TYPE)
        if (roomId != null && roomType != null) {
            getRoomInfo(roomId, roomType)
            tvRoomId.text = roomId
        }

    }

    private fun getRoomInfo(roomId : String, roomType : String) {
        databaseReference.child(FirebaseConst.ROOMS).child(roomType).child(roomId)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    room = it.getValue(Room::class.java)!!
                    supportActionBar?.title = room.name
                }
            }
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