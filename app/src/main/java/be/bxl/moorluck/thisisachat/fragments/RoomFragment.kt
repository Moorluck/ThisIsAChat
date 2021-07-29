package be.bxl.moorluck.thisisachat.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.R
import be.bxl.moorluck.thisisachat.adapters.RoomAdapter
import be.bxl.moorluck.thisisachat.models.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class RoomFragment : Fragment() {

    // Fire Base
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference: StorageReference

    // View

    lateinit var rvRoom : RecyclerView

    // Adapter

    lateinit var roomAdapter : RoomAdapter

    // Room data

    var rooms : MutableList<Room> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_room, container, false)

        // Firebase
        auth = Firebase.auth
        databaseReference = Firebase
            .database("https://thisisachat-b0f70-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
        storageReference = Firebase
            .storage("gs://thisisachat-b0f70.appspot.com")
            .reference

        // Set up the recycler view

        rvRoom = v.findViewById(R.id.rv_room_fragment)
        rvRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        databaseReference.child("users").child(auth.currentUser!!.uid).child("rooms").get()
            .addOnSuccessListener { roomList ->

                val listOfRooms : MutableList<Room> = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child("rooms").child(room.value.toString()).get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                listOfRooms.add(it.getValue(Room::class.java)!!)
                                roomAdapter = RoomAdapter()
                                roomAdapter.rooms = listOfRooms
                                rvRoom.adapter = roomAdapter
                            }
                        }
                }


            }

        return v
    }

    companion object {
        @JvmStatic
        fun newInstance() = RoomFragment()
    }
}