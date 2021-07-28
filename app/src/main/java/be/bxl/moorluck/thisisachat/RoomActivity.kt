package be.bxl.moorluck.thisisachat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import be.bxl.moorluck.thisisachat.fragments.PrivateFragment
import be.bxl.moorluck.thisisachat.fragments.RoomFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RoomActivity : AppCompatActivity() {

    // Firebase

    private lateinit var auth : FirebaseAuth

    // View

    private lateinit var bottomNav : BottomNavigationView

    // Fragment
    private lateinit var fragmentManager: FragmentManager
    private lateinit var roomFragment: Fragment
    private lateinit var privateFragment : Fragment

    // User Data from firebase

    lateinit var user : FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        // Firebase

        auth = Firebase.auth

        // Check if the user is logged in

        initializeUserFromFirebase()

        // View

        bottomNav = findViewById(R.id.bottom_nav_room_activity)

        // Fragment manage and initial fragment
        fragmentManager = supportFragmentManager
        roomFragment = RoomFragment.newInstance()
        privateFragment = PrivateFragment.newInstance()

        fragmentManager.beginTransaction()
            .replace(R.id.fl_fragment_container_room_activity, roomFragment)
            .commit()



        // Initialize bottom nav

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_room -> {
                    fragmentManager.beginTransaction()
                        .replace(R.id.fl_fragment_container_room_activity, roomFragment)
                        .commit()
                    true
                }
                R.id.menu_private -> {
                    fragmentManager.beginTransaction()
                        .replace(R.id.fl_fragment_container_room_activity, privateFragment)
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.room_menu_action_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_profile -> {
                val intent = Intent(this, ManageProfileActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initializeUserFromFirebase() {

        if (auth.uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        if (auth.currentUser != null) {
            user = auth.currentUser!!
        }

    }
}