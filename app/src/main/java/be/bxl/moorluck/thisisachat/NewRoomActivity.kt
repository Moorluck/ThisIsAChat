package be.bxl.moorluck.thisisachat

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Grade
import be.bxl.moorluck.thisisachat.models.Room
import be.bxl.moorluck.thisisachat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*

class NewRoomActivity : Activity() {

    companion object {
        const val REQUEST_CODE = 1
    }

    // Firebase

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference : StorageReference

    // View

    private lateinit var tvAddPicture : TextView
    private lateinit var etName : EditText
    private lateinit var btnCreate : Button
    private lateinit var imgRoom : ImageView

    // Uri

    var uri : Uri? = null

    // User

    lateinit var user : User

    // Room info

    private lateinit var roomId : String
    private lateinit var photoRef : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_room)

        // FireBase

        auth = Firebase.auth
        databaseReference = Firebase
            .database(FirebaseConst.URL_DATABASE)
            .reference
        storageReference = Firebase.storage(FirebaseConst.URL_STORAGE).reference

        //View

        tvAddPicture = findViewById(R.id.tv_add_image_new_room_activity)
        etName = findViewById(R.id.et_name_room_activity)
        btnCreate = findViewById(R.id.btn_create_room_activity)
        imgRoom = findViewById(R.id.img_new_room_activity)

        // Setup rounded corner

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Get user info

        getUserInfo()

        btnCreate.setOnClickListener {
            if (etName.text.toString().trim().isNotEmpty() && uri != null) {
                btnCreate.isEnabled = false
                uploadImg()
            }
            else {
                Toast.makeText(this, "Your room must have a name and a picture", Toast.LENGTH_SHORT).show()
            }
        }

        imgRoom.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private fun getUserInfo() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                user = it.getValue(User::class.java)!!
            }
    }

    private fun uploadImg() {
        // Upload image then register the user (can't do the opposite)
        val fileName = UUID.randomUUID()
        val imageRef = storageReference.child("images/$fileName")

        imageRef.putFile(uri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {
                    photoRef = it.toString()
                    createNewRoom()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error while uploading image : $it", Toast.LENGTH_LONG).show()
            }

    }



    private fun createNewRoom() {
        val roomName = etName.text.toString()
        roomId = UUID.randomUUID().toString()
        val room = Room(
            type = FirebaseConst.CUSTOM,
            id = roomId,
            name = roomName,
            users = mapOf(auth.currentUser!!.uid to user.pseudo!!),
            photoRef = photoRef,
            grades = mapOf(Grade().name to Grade(users = mapOf(auth.currentUser!!.uid to user.pseudo!!)))
        )

        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.CUSTOM).child(roomId).setValue(room)
            .addOnSuccessListener {
                addRoomToUserInfo()
            }
    }

    private fun addRoomToUserInfo() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.ROOMS)
            .child(roomId).setValue(true)
            .addOnSuccessListener {
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            uri = data!!.data!!
            tvAddPicture.visibility = View.INVISIBLE
            val source = ImageDecoder.createSource(contentResolver, uri!!)
            val profileDrawable = ImageDecoder.decodeDrawable(source)
            imgRoom.setImageDrawable(profileDrawable)
            imgRoom.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}