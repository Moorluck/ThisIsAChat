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
import be.bxl.moorluck.thisisachat.api.Url
import be.bxl.moorluck.thisisachat.api.models.Place
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
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
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.*

class SignUpActivity : AppCompatActivity() {

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

    private var _placeRooms : MutableList<String> = mutableListOf()
    private var _hobbyRooms : MutableList<String> = mutableListOf()
    private var _newPlaceRooms : MutableList<Room> = mutableListOf()
    private var _newHobbyRooms : MutableList<Room> = mutableListOf()
    private var rooms : MutableMap<String, String> = mutableMapOf()

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
            .database(FirebaseConst.URL_DATABASE)
            .reference
        storageReference = Firebase.storage(FirebaseConst.URL_STORAGE).reference

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

        // On click

        btnSignUp.setOnClickListener {


            email = etEmail.text.toString()
            password = etPassword.text.toString()
            pseudo = etPseudo.text.toString()

            if (email.trim() != "" && password.trim() != "" && pseudo.trim() != "") {
                // Get position then create or join the rooms
                val locationHelper = LocationHelper(this) { pos ->
                    btnSignUp.isEnabled = false
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

    // Get place with geocoding api

    private fun executeCallToGetPlace() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val response = RetrofitInstance.apiGeocoding.getPlace(
                    latLong.lat,
                    latLong.long
                )
                getInitialRoomByPlaces(response)
            }
            catch (e: Exception) {
                Toast.makeText(this@SignUpActivity, "Error calling api : $e", Toast.LENGTH_LONG).show()
            }

        }
    }

    // Add place to rooms

    private fun getInitialRoomByPlaces(response : Place) {

        country = response.address.country?.replace("/", "-")
        state = response.address.state?.replace("/", "-")
            ?: response.address.region?.replace("/", "-")
            ?: response.address.county?.replace("/", "-")
        city = response.address.city?.replace("/", "-")
            ?: response.address.village?.replace("/", "-")
            ?: response.address.town?.replace("/", "-")

        if (country != null && state != null && city != null) {
            _placeRooms.add(country!!)
            _placeRooms.add(state!!)
            _placeRooms.add(city!!)
            getInitialRoomByHobby()
        }
        else {
            Toast.makeText(this@SignUpActivity, "Error generating rooms", Toast.LENGTH_LONG).show()
        }
    }

    // Add hobbies to rooms

    private fun getInitialRoomByHobby() {
        if (cbCooking.isChecked) {
            _hobbyRooms.add(cbCooking.text.toString())
        }

        if (cbMusic.isChecked) {
            _hobbyRooms.add(cbMusic.text.toString())
        }

        if (cbTravelling.isChecked) {
            _hobbyRooms.add(cbTravelling.text.toString())
        }

        if (cbPhotography.isChecked) {
            _hobbyRooms.add(cbPhotography.text.toString())
        }

        if (cbDrawing.isChecked) {
            _hobbyRooms.add(cbDrawing.text.toString())
        }

        if (cbScience.isChecked) {
            _hobbyRooms.add(cbScience.text.toString())
        }

        if (cbVideoGames.isChecked) {
            _hobbyRooms.add(cbVideoGames.text.toString())
        }

        if (cbSport.isChecked) {
            _hobbyRooms.add(cbSport.text.toString())
        }

        uploadImg()
    }

    // Upload the image

    private fun uploadImg() {
        // Upload image then register the user (can't do the opposite)
        if (uri != null) {
            val fileName = UUID.randomUUID()
            val imageRef = storageReference.child("images/$fileName")

            imageRef.putFile(uri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener {
                        imgUrl = it.toString()
                        registerUser()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while uploading image : $it", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Register the user

    private fun registerUser() {

        // Signing up user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    userId = it.result?.user!!.uid
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            generateHobbyRooms()
                        }
                        .addOnFailureListener {
                            Log.d("ERROR", "Unable to sign in")
                        }
                }
                else {
                    Toast.makeText(this, "Error while signing up ${it.exception}", Toast.LENGTH_LONG).show()
                    btnSignUp.isEnabled = true
                }
        }
    }

    // Add a user when the room already exist

    private fun addUserToARoom(roomName : String, type : String) {


        databaseReference.child(FirebaseConst.ROOMS)
            .child(type)
            .child(roomName)
            .child(FirebaseConst.USERS)
            .child(userId)
            .setValue(pseudo)

        databaseReference.child(FirebaseConst.ROOMS)
            .child(type)
            .child(roomName)
            .child(FirebaseConst.GRADES)
            .child(Grade().name)
            .child(FirebaseConst.USERS)
            .child(userId)
            .setValue(pseudo)
    }

    // Generate all the hobby rooms

    private fun generateHobbyRooms() {

        if (_hobbyRooms.isNotEmpty()) {
            for ((index, room) in _hobbyRooms.withIndex()) {

                databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.HOBBY).child(room).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            if (index == _hobbyRooms.size - 1) {

                                for (hobbyRoom in _hobbyRooms) {
                                    rooms[UUID.randomUUID().toString()] = hobbyRoom
                                }

                                lifecycleScope.launch(Dispatchers.Main) {
                                    addUserToARoom(room, "hobby")
                                    generatePlacesRooms()
                                }
                            }
                            else {
                                addUserToARoom(room, "hobby")
                            }
                        }
                        else {
                            lifecycleScope.launch(Dispatchers.Main) {
                                try {
                                    val photoRef = "https://firebasestorage.googleapis.com/v0/b/thisisachat-b0f70.appspot.com/o/images%2Fhobby.jpg?alt=media&token=08b97907-2d3a-43c4-a200-d0d821e9dfab"
                                    _newHobbyRooms.add(
                                        Room(name = room, id = room, photoRef = photoRef, type = "hobby")
                                    )
                                }
                                catch(e : Exception)  {
                                    Toast.makeText(this@SignUpActivity, "Error while loading place photo : $e", Toast.LENGTH_LONG).show()
                                }

                                if (index == _hobbyRooms.size - 1) {

                                    for (hobbyRoom in _hobbyRooms) {
                                        rooms[UUID.randomUUID().toString()] = hobbyRoom
                                    }

                                    generatePlacesRooms()
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error while accessing database", Toast.LENGTH_LONG).show()
                    }
            }
        }
        else {
            generatePlacesRooms()
        }

    }

    // Generate all the places rooms

    private fun generatePlacesRooms() {
        for ((index, room) in _placeRooms.withIndex()) {

            databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PLACE).child(room).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        if (index == _placeRooms.size - 1) {

                            for (placeRoom in _placeRooms) {
                                rooms[UUID.randomUUID().toString()] = placeRoom
                            }

                            lifecycleScope.launch(Dispatchers.Main) {
                                addUserToARoom(room, "place")
                                addUserToDataBase()
                            }
                        }
                        else {
                            addUserToARoom(room, "place")
                        }
                    }
                    else {
                        runBlocking {
                            try {
                                val result = RetrofitInstance.apiPlace.getPlacesDetail(room, Url.getApiKey(this@SignUpActivity))

                                _newPlaceRooms.add(
                                    Room(name = room, id = room, photoRef = result.results[0].photos[0].photo_reference, type = "place")
                                )
                            }
                            catch(e : Exception)  {

                                _newPlaceRooms.add(
                                    Room(name = room, id = room, photoRef = "", type = "place")
                                )

                                Toast.makeText(this@SignUpActivity, "Error while loading place photo : $e", Toast.LENGTH_LONG).show()
                            }
                        }

                        if (index == _placeRooms.size - 1) {

                            for (placeRoom in _placeRooms) {
                                rooms[UUID.randomUUID().toString()] = placeRoom
                            }

                            addUserToDataBase()

                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while accessing database", Toast.LENGTH_LONG).show()
                }
        }

    }

    // Add the user to DB

    private fun addUserToDataBase() {
        val user = User(email, pseudo, rooms.toMap(), imgUrl, latLong)
        // Add user data in real-time database
        databaseReference.child(FirebaseConst.USERS).child(userId).setValue(user)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    uploadingNewRooms()
                }
                else {
                    Toast.makeText(this, "Error while updating database", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error while updating database", Toast.LENGTH_LONG).show()
            }
    }

    // Add new roooms to DB

    private fun uploadingNewRooms() {
        // Add new rooms to database
        for (room in _newPlaceRooms) {
            val newMapOfUser = mapOf(userId to pseudo)
            val newMapOfGrade = mapOf(Grade().name to Grade(users = mapOf(userId to pseudo)))
            room.users = newMapOfUser.toMap()
            room.grades = newMapOfGrade

            runBlocking {
                databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PLACE)
                    .child(room.name!!).setValue(room)
            }
        }

        for (room in _newHobbyRooms) {
            val newMapOfUser = mapOf(userId to pseudo)
            val newMapOfGrade = mapOf(Grade().name to Grade(users = mapOf(userId to pseudo)))
            room.users = newMapOfUser.toMap()
            room.grades = newMapOfGrade

            runBlocking {
                databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.HOBBY)
                    .child(room.name!!).setValue(room)
            }
        }

        val intent = Intent(this, RoomActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


}