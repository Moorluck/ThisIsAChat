package be.bxl.moorluck.thisisachat

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Room
import be.bxl.moorluck.thisisachat.models.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*

class ManageProfileActivity : AppCompatActivity() {

    //Firebase

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    // View
    private lateinit var tvId : TextView
    private lateinit var etPseudo : EditText
    private lateinit var cbMusic : CheckBox
    private lateinit var cbTravelling : CheckBox
    private lateinit var cbPhotography : CheckBox
    private lateinit var cbDrawing : CheckBox
    private lateinit var cbScience : CheckBox
    private lateinit var cbVideoGames : CheckBox
    private lateinit var cbCooking : CheckBox
    private lateinit var cbSport : CheckBox
    private lateinit var btnModify : Button
    private lateinit var btnSignOut : Button
    private lateinit var imgProfile : ImageView

    // User

    private var userFirebase : FirebaseUser? = null
    private var user : User? = null

    // Uri and imgurl

    private var uri : Uri? = null
    private var imgUrl : String = ""

    // Activity for result

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            uri = it.data?.data
            if (uri != null) {
                val source = ImageDecoder.createSource(contentResolver, uri!!)
                val profileDrawable = ImageDecoder.decodeDrawable(source)
                imgProfile.setImageDrawable(profileDrawable)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_profile)

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
        tvId = findViewById(R.id.tv_id_manage_profile_activity)

        etPseudo = findViewById(R.id.et_pseudo_manage_profile_activity)

        cbMusic = findViewById(R.id.cb_music_manage_profile_activity)
        cbTravelling = findViewById(R.id.cb_travel_manage_profile_activity)
        cbPhotography = findViewById(R.id.cb_photography_manage_profile_activity)
        cbDrawing = findViewById(R.id.cb_drawing_manage_profile_activity)
        cbScience = findViewById(R.id.cb_science_manage_profile_activity)
        cbVideoGames = findViewById(R.id.cb_video_games_manage_profile_activity)
        cbCooking = findViewById(R.id.cb_cooking_manage_profile_activity)
        cbSport = findViewById(R.id.cb_sport_manage_profile_activity)

        btnModify = findViewById(R.id.btn_modify_manage_profile_activity)
        btnSignOut = findViewById(R.id.btn_sign_out_maage_profile_activity)

        imgProfile = findViewById(R.id.img_profile_manage_profile_activity)

        // Set up id

        val idText = "ID : " + userFirebase!!.uid
        tvId.text = idText

        setupCheckBoxes()

        // OnClick

        btnModify.setOnClickListener {
            uploadImg()
        }

        imgProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }

        btnSignOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // Action bar title

        supportActionBar?.title = "Manage Profile"

        if (userFirebase != null) {
            databaseReference.child(FirebaseConst.USERS).child(userFirebase!!.uid).get()
                .addOnSuccessListener {
                    user = it.getValue(User::class.java)

                    updateUI()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while loading user : $it", Toast.LENGTH_LONG).show()
                }
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
                        updatePseudo()
                    }
                }
        }
        else {
            updatePseudo()
        }
    }

    private fun updatePseudo() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.PSEUDO)
            .setValue(etPseudo.text.toString())
            .addOnSuccessListener {
                updateImgUrl()
            }
    }

    private fun updateImgUrl() {
        if (imgUrl != "") {
            databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.IMG_URL)
                .setValue(imgUrl)
                .addOnSuccessListener {
                    finish()
                }
        }
        else {
            finish()
        }
    }

    private fun setupCheckBoxes() {
        databaseReference.child(FirebaseConst.USERS).child(userFirebase!!.uid).child(FirebaseConst.ROOMS)
            .get()
            .addOnSuccessListener { roomList ->
                roomList.children.forEach {
                    val roomId = it.key

                    if (roomId == getString(R.string.music)) {
                        cbMusic.isChecked = true
                    }

                    if (roomId == getString(R.string.travelling)) {
                        cbTravelling.isChecked = true
                    }

                    if (roomId == getString(R.string.photography)) {
                        cbPhotography.isChecked = true
                    }

                    if (roomId == getString(R.string.drawing)) {
                        cbDrawing.isChecked = true
                    }

                    if (roomId == getString(R.string.science)) {
                        cbScience.isChecked = true
                    }

                    if (roomId == getString(R.string.video_games)) {
                        cbVideoGames.isChecked = true
                    }

                    if (roomId == getString(R.string.cooking)) {
                        cbCooking.isChecked = true
                    }

                    if (roomId == getString(R.string.sports)) {
                        cbSport.isChecked = true
                    }
                }
            }
    }

    private fun updateUI() {
        etPseudo.setText(user?.pseudo)
        Glide.with(this)
            .load(user?.imgUrl)
            .centerCrop()
            .into(imgProfile)
    }
}