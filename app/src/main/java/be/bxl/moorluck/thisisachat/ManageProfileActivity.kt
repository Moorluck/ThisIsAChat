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
import android.util.Log
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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    private var listOfCb : MutableList<CheckBox> = mutableListOf()
    private lateinit var btnModify : Button
    private lateinit var btnSignOut : Button
    private lateinit var imgProfile : ImageView
    private lateinit var imgCopy : ImageView
    private lateinit var imgUpdateLocation : ImageView

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

        listOfCb.addAll(listOf(cbMusic, cbTravelling, cbPhotography, cbDrawing, cbScience,
            cbVideoGames, cbCooking, cbSport))

        btnModify = findViewById(R.id.btn_modify_manage_profile_activity)
        btnSignOut = findViewById(R.id.btn_sign_out_maage_profile_activity)

        imgProfile = findViewById(R.id.img_profile_manage_profile_activity)
        imgCopy = findViewById(R.id.img_copy_manage_profile_activity)
        imgUpdateLocation = findViewById(R.id.img_update_location_manage_profile_activity)

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

        imgCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(ManageRoomActivity.LABEL_COPY, auth.currentUser!!.uid)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "User ID successfully copied !", Toast.LENGTH_LONG).show()
        }

        imgUpdateLocation.setOnClickListener {
            updateLocation()
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

    private fun updateLocation() {
        val locationHelper = LocationHelper(this) { pos ->
            updateLatLong(pos)
        }
        locationHelper.getLastLocation()
    }

    private fun updateLatLong(pos: Position) {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.POSITION).setValue(pos)
            .addOnSuccessListener {
                executeCallToGetPlace(pos)
            }
    }

    private fun executeCallToGetPlace(pos: Position) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val response = RetrofitInstance.apiGeocoding.getPlace(
                    pos.lat,
                    pos.long
                )
                getRoomByPlaces(response)
            }
            catch (e: Exception) {
                Toast.makeText(this@ManageProfileActivity, "Error calling api : $e", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun getRoomByPlaces(response: Place) {

        val country = response.address.country?.replace("/", "-")
        val state = response.address.state?.replace("/", "-")
            ?: response.address.region?.replace("/", "-")
            ?: response.address.county?.replace("/", "-")
        val city = response.address.city?.replace("/", "-")
            ?: response.address.village?.replace("/", "-")
            ?: response.address.town?.replace("/", "-")

        Log.d("place", country + state + city)

        if (country != null) {
            getPlaceRoom(country)
        }
        if (state != null) {
            getPlaceRoom(state)
        }
        if (city != null) {
            getPlaceRoom(city)
        }
    }

    private fun getPlaceRoom(place: String) {
        databaseReference.child(FirebaseConst.USERS).child(FirebaseConst.ROOMS).child(place).get()
            .addOnSuccessListener {
                if (!it.exists()) {
                    runBlocking {
                        generateRoom(place)
                    }
                }
            }
    }

    private fun generateRoom(place: String) {
        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PLACE).child(place).get()
            .addOnSuccessListener {
                if (!it.exists()) {
                    runBlocking {
                        try {
                            val result = RetrofitInstance.apiPlace.getPlacesDetail(place, Url.getApiKey(this@ManageProfileActivity))

                            createNewPlaceRoom(
                                Room(name = place, id = place, photoRef = result.results[0].photos[0].photo_reference, type = "place")
                            )

                            databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.ROOMS)
                                .child(place).setValue(false)


                        }
                        catch(e : Exception)  {

                            createNewPlaceRoom(
                                Room(name = place, id = place, photoRef = "", type = "place")
                            )

                            Toast.makeText(this@ManageProfileActivity, "Error while loading place photo : $e", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                else {
                    databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.ROOMS)
                        .child(place).setValue(false)
                }
            }
    }

    private fun createNewPlaceRoom(room: Room) {
        Log.d("heelo", "hello")
        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PLACE).child(room.id!!)
            .setValue(room)
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
                    updateHobbyRoom()
                }
        }
        else {
            updateHobbyRoom()
        }
    }

    private fun updateHobbyRoom() {
        runBlocking {
            for (cb in listOfCb) {
                if (cb.isChecked) {
                    checkIfUserAlreadyJoinTheRoom(cb.text.toString())
                }
                else {
                    checkIfUserWantToRemoveTheRoom(cb.text.toString())
                }
            }
        }

        finish()

    }

    private fun checkIfUserWantToRemoveTheRoom(cbText: String) {
        databaseReference.child(FirebaseConst.USERS).child(userFirebase!!.uid).child(FirebaseConst.ROOMS)
            .child(cbText)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    removeRoomFromUser(cbText)
                }
            }
    }

    private fun removeRoomFromUser(cbText: String) {
        databaseReference.child(FirebaseConst.USERS).child(userFirebase!!.uid).child(FirebaseConst.ROOMS)
            .child(cbText)
            .removeValue()
            .addOnSuccessListener {
                removeUserFromRoom(cbText)
            }
    }

    private fun removeUserFromRoom(cbText: String) {
        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.HOBBY).child(cbText)
            .child(FirebaseConst.USERS).child(userFirebase!!.uid)
            .removeValue()
    }

    private fun checkIfUserAlreadyJoinTheRoom(cbText: String) {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid)
            .child(FirebaseConst.ROOMS).child(cbText).get()
            .addOnSuccessListener {
                if (!it.exists()) {
                    addRoomToUser(cbText)
                }
            }
    }

    private fun addRoomToUser(cbText: String) {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid)
            .child(FirebaseConst.ROOMS).child(cbText).setValue(false)
            .addOnSuccessListener {
                checkIfRoomExist(cbText)
            }
    }

    private fun checkIfRoomExist(cbText: String) {
        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.HOBBY).child(cbText)
            .get()
            .addOnSuccessListener {
                if (!it.exists()) {
                    createNewHobbyRoom(cbText)
                }

                else {
                    addUserToRoom(cbText, FirebaseConst.HOBBY)
                }
            }
    }

    private fun addUserToRoom(cbText: String, type : String) {
        databaseReference.child(FirebaseConst.ROOMS).child(type).child(cbText)
            .child(FirebaseConst.USERS)
            .child(userFirebase!!.uid)
            .setValue(user!!.pseudo)
    }

    private fun createNewHobbyRoom(cbText: String) {

        val newMapOfUser = mapOf(userFirebase!!.uid to user!!.pseudo!!)
        val newMapOfGrade = mapOf(Grade().name to Grade(users = mapOf(userFirebase!!.uid to user!!.pseudo!!)))

        val newRoom = Room(type = FirebaseConst.HOBBY,
                        id = cbText,
                        name = cbText,
                        users = newMapOfUser,
                        photoRef = "https://firebasestorage.googleapis.com/v0/b/thisisachat-b0f70.appspot.com/o/images%2Fhobby.jpg?alt=media&token=08b97907-2d3a-43c4-a200-d0d821e9dfab",
                        grades = newMapOfGrade
        )

        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.HOBBY).child(cbText)
            .setValue(newRoom)
    }

    private fun setupCheckBoxes() {
        databaseReference.child(FirebaseConst.USERS).child(userFirebase!!.uid).child(FirebaseConst.ROOMS)
            .get()
            .addOnSuccessListener { roomList ->
                roomList.children.forEach {
                    val roomId = it.key

                    for (cb in listOfCb) {
                        if (roomId == cb.text.toString()) {
                            cb.isChecked = true
                        }
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