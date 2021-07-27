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
import be.bxl.moorluck.thisisachat.models.Position
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

class SignUpActivity : AppCompatActivity() {

    // Const

    companion object {
        val USER_ID_CODE = "USER_ID_CODE"
        val USER_EMAIL_CODE = "USER_EMAIL_CODE"
        val USER_PSEUDO_CODE = "USER_PSEUDO_CODE"
    }

    // Firebase

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference: StorageReference

    // View

    lateinit var tvAddPicture : TextView

    lateinit var etEmail : EditText
    lateinit var etPassword : EditText
    lateinit var etPseudo : EditText

    lateinit var cbMusic : CheckBox
    lateinit var cbTravelling : CheckBox
    lateinit var cbPhotography : CheckBox
    lateinit var cbDrawing : CheckBox
    lateinit var cbScience : CheckBox
    lateinit var cbVideoGames : CheckBox
    lateinit var cbCooking : CheckBox
    lateinit var cbSport : CheckBox

    lateinit var btnSignUp : Button

    lateinit var imgProfile : ImageView

    // Photo URI

    private var uri : Uri? = null
    private lateinit var imgUrl : String

    // activity for result to pick a photo (need to be outside of the activity)

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
        setContentView(R.layout.activity_sign_up)

        // FireBase

        auth = Firebase.auth
        databaseReference = Firebase
            .database("https://thisisachat-b0f70-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
        storageReference = Firebase.storage("gs://thisisachat-b0f70.appspot.com").reference

        // View

        tvAddPicture = findViewById(R.id.tv_addpicture_signup_activity)

        etEmail = findViewById(R.id.et_e_mail_signup_activity)
        etPassword = findViewById(R.id.et_password_signup_activity)
        etPseudo = findViewById(R.id.et_pseudo_signup_activity)

        cbMusic = findViewById(R.id.cb_music_signup_activity)
        cbTravelling = findViewById(R.id.cb_travel_signup_activity)
        cbPhotography = findViewById(R.id.cb_photography_signup_activity)
        cbDrawing = findViewById(R.id.cb_drawing_signup_activity)
        cbScience = findViewById(R.id.cb_science_signup_activity)
        cbVideoGames = findViewById(R.id.cb_video_games_signup_activity)
        cbCooking = findViewById(R.id.cb_cooking_signup_activity)
        cbSport = findViewById(R.id.cb_sport_signup_activity)

        btnSignUp = findViewById(R.id.btn_signup_signup_activity)

        imgProfile = findViewById(R.id.img_profile_signup_activity)

        btnSignUp.setOnClickListener {

            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val pseudo = etPseudo.text.toString()

            if (email.trim() != "" && password.trim() != "" && pseudo.trim() != "") {
                uploadImgAndAddUserToDatabase(uri, email, password, pseudo)
            }
            else {
                Toast.makeText(this, "You must fill all the fields", Toast.LENGTH_LONG).show()
            }
        }

        imgProfile.setOnClickListener {
            tvAddPicture.visibility = View.INVISIBLE

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }
    }

    private fun uploadImgAndAddUserToDatabase(uri : Uri? = null, email : String, password : String, pseudo : String) {
        if (uri != null) {
            val fileName = UUID.randomUUID()
            val imageRef = storageReference.child("images/$fileName")

            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener {
                        Log.d("URL", it.toString())
                        imgUrl = it.toString()
                        registerUser(email, password, pseudo, imgUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while uploading image", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun registerUser(email : String, password : String, pseudo : String, imgUrl : String) {

        // Signing up user

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    val latLong : Position = manageLocalization()
                    val rooms = getInitialRoomByHobbies()
                    val user = User(email, pseudo, rooms, imgUrl, latLong)

                    val fireBaseUserId : String = it.result?.user!!.uid

                    // Add user data in real-time database
                    databaseReference.child("users").child(fireBaseUserId).setValue(user)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "Sign up !", Toast.LENGTH_LONG).show()

                                val intent = Intent(this, RoomActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else {
                                Toast.makeText(this, "Error while updating database", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error while signing uupdating database", Toast.LENGTH_LONG).show()
                        }

                }
                else {
                    Toast.makeText(this, "Error while signing up", Toast.LENGTH_LONG).show()
                }
        }
        // uploadImg(imgProfile.drawable as Bitmap)
    }

    private fun getInitialRoomByHobbies(): List<Room> {
        //TODO get room checking the ceckboxes
        return listOf()
    }

    private fun manageLocalization(): Position {
        //TODO find localization with reverse geocoding
        return Position()
    }


}