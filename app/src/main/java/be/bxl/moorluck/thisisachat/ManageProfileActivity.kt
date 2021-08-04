package be.bxl.moorluck.thisisachat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
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

class ManageProfileActivity : AppCompatActivity() {

    //Firebase

    lateinit var auth : FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var storageReference: StorageReference

    // View
    lateinit var etPseudo : EditText

    lateinit var cbMusic : CheckBox
    lateinit var cbTravelling : CheckBox
    lateinit var cbPhotography : CheckBox
    lateinit var cbDrawing : CheckBox
    lateinit var cbScience : CheckBox
    lateinit var cbVideoGames : CheckBox
    lateinit var cbCooking : CheckBox
    lateinit var cbSport : CheckBox

    lateinit var btnModify : Button
    lateinit var btnSignOut : Button

    lateinit var imgProfile : ImageView

    // User

    var userFirebase : FirebaseUser? = null
    var user : User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_profile)

        // Firebase

        auth = Firebase.auth
        userFirebase = auth.currentUser
        databaseReference = Firebase
            .database("https://thisisachat-b0f70-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
        storageReference = Firebase
            .storage("gs://thisisachat-b0f70.appspot.com")
            .reference

        // View
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

        // OnClick

        btnModify.setOnClickListener {
            //TODO gestion modification du pseudo
            return@setOnClickListener
        }

        imgProfile.setOnClickListener {
            //TODO gestion suppression et ajout d'image
            return@setOnClickListener
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
            databaseReference.child("users").child(userFirebase!!.uid).get()
                .addOnSuccessListener {
                    user = it.getValue(User::class.java)

                    updateUI()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error while loading user : $it", Toast.LENGTH_LONG).show()
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