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
import androidx.lifecycle.lifecycleScope
import be.bxl.moorluck.thisisachat.api.RetrofitInstance
import be.bxl.moorluck.thisisachat.api.models.Place
import be.bxl.moorluck.thisisachat.helpers.LocationHelper
import be.bxl.moorluck.thisisachat.models.Grade
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
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

    // User Info

    private lateinit var userId : String

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var pseudo: String
    private lateinit var imgUrl : String
    private lateinit var latLong : Position

    private var _rooms : MutableList<String> = mutableListOf()
    private var _newRooms : MutableList<Room> = mutableListOf()
    private lateinit var rooms : List<String>

    var country : String? = null
    var state : String? = null
    var city : String? = null

    // activity for result to pick a photo (need to be outside of the activity)

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            uri = it.data?.data
            if (uri != null) {
                tvAddPicture.visibility = View.INVISIBLE
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

            email = etEmail.text.toString()
            password = etPassword.text.toString()
            pseudo = etPseudo.text.toString()

            if (email.trim() != "" && password.trim() != "" && pseudo.trim() != "") {
                // Get position then create or join the rooms
                val locationHelper = LocationHelper(this) { pos ->
                    Log.d("Position", "woups")
                    latLong = pos
                    executeCallToGetPlace()
                }
                locationHelper.getLastLocation()
            }
            else {
                Toast.makeText(this, "You must fill all the fields", Toast.LENGTH_LONG).show()
            }
        }

        imgProfile.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }
    }

    private fun executeCallToGetPlace() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val response = RetrofitInstance.api.getPlace(
                    latLong.lat,
                    latLong.long
                )
                getInitialRoomByHobbies(response)
            }
            catch (e: Exception) {
                Toast.makeText(this@SignUpActivity, "Error calling api : $e", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun getInitialRoomByHobbies(response : Place) {
        country = response.address.country
        state = response.address.state ?: response.address.region ?: response.address.county
        city = response.address.city ?: response.address.village ?: response.address.town

        Log.d("PLACE", "$country $state $city")

        if (country != null && state != null && city != null) {
            uploadImg()
        }
        else {
            Toast.makeText(this@SignUpActivity, "Error generating rooms", Toast.LENGTH_LONG).show()
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
                        Log.d("URL", it.toString())
                        imgUrl = it.toString()
                        registerUser()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while uploading image : $it", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun registerUser() {

        // Signing up user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    userId = it.result?.user!!.uid
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            generatePlaceRooms()
                        }
                        .addOnFailureListener {
                            Log.d("ERROR", "Unable to sign in")
                        }
                }
                else {
                    Toast.makeText(this, "Error while signing up ${it.exception}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun addUserToARoom(roomName : String) {
        databaseReference.child("rooms")
            .child(roomName)
            .child("users")
            .child(userId)
            .setValue(pseudo)

        databaseReference.child("rooms")
            .child(roomName)
            .child("grades")
            .child("0")
            .child("users")
            .get()
            .addOnSuccessListener { users ->
                val listOfUserIdAtGrade : MutableList<String> = mutableListOf()
                users.children.forEach { user ->
                    listOfUserIdAtGrade.add(user.value.toString())
                }

                listOfUserIdAtGrade.add(userId)

                databaseReference.child("rooms")
                    .child(roomName)
                    .child("grades")
                    .child("0")
                    .child("users")
                    .setValue(listOfUserIdAtGrade)
            }
    }

    private fun generatePlaceRooms() {
        databaseReference.child("rooms").child(country!!).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    addUserToARoom(country!!)

                    _rooms.add(country!!)
                    generateStateRoom()
                }
                else {
                    _rooms.add(country!!)
                    _newRooms.add(
                        Room(name = country)
                    )
                    generateStateRoom()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error while accessing database", Toast.LENGTH_LONG).show()
            }
    }

    private fun generateStateRoom() {
        databaseReference.child("rooms").child(state!!).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    addUserToARoom(state!!)

                    _rooms.add(state!!)
                    generateCityRoom()
                }
                else {
                    _rooms.add(state!!)
                    _newRooms.add(
                        Room(name = state)
                    )
                    generateCityRoom()
                }

            }
            .addOnFailureListener {
                Toast.makeText(this, "Error while accessing database", Toast.LENGTH_LONG).show()
            }
    }

    private fun generateCityRoom() {
        databaseReference.child("rooms").child(city!!).get()
            .addOnSuccessListener { it ->
                if (it.exists()) {
                    addUserToARoom(city!!)

                    _rooms.add(city!!)
                    rooms = _rooms.toList()

                    addUserToDataBase()
                }
                else {
                    _newRooms.add(
                        Room(name = city)
                    )

                    _rooms.add(city!!)
                    rooms = _rooms.toList()

                    addUserToDataBase()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error while accessing database", Toast.LENGTH_LONG).show()
            }
    }

    private fun addUserToDataBase() {
        val user = User(email, pseudo, rooms, imgUrl, latLong)
        // Add user data in real-time database
        databaseReference.child("users").child(userId).setValue(user)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    uploadingNewRooms()
                }
                else {
                    Toast.makeText(this, "Error while updating database", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error while signing updating database", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadingNewRooms() {
        for (room in _newRooms) {
            val newMapOfUser = mapOf(userId to pseudo)
            val newListOfGrade = listOf(Grade(users = listOf(userId)))
            room.users = newMapOfUser.toMap()
            room.grades = newListOfGrade

            databaseReference.child("rooms").child(room.name!!).setValue(room)
        }

        val intent = Intent(this, RoomActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


}