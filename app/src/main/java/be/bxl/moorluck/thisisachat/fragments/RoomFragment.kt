package be.bxl.moorluck.thisisachat.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.ChatActivity
import be.bxl.moorluck.thisisachat.NewRoomActivity
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

class RoomFragment : Fragment(), RoomAdapter.ItemClickListener {

    // Companion object

    companion object {
        @JvmStatic
        fun newInstance() = RoomFragment()

        const val ROOM_NAME = "ROOM_NAME"
    }

    // Fire Base
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference: StorageReference

    // View

    lateinit var rvRegionRoom : RecyclerView
    lateinit var rvHobbyRoom : RecyclerView
    lateinit var rvCustomRoom : RecyclerView

    lateinit var btnNewRoom : Button

    // Adapter

    private lateinit var roomRegionAdapter : RoomAdapter
    lateinit var roomHobbyAdapter : RoomAdapter
    lateinit var roomCustomAdapter : RoomAdapter

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

        // View

        rvRegionRoom = v.findViewById(R.id.rv_region_room_fragment)
        rvHobbyRoom = v.findViewById(R.id.rv_hobby_room_fragment)
        rvCustomRoom = v.findViewById(R.id.rv_custom_room_fragment)

        btnNewRoom = v.findViewById(R.id.btn_new_room_room_fragment)

        // Set up recycler views
        rvRegionRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvHobbyRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvCustomRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        roomRegionAdapter = RoomAdapter(requireContext(), this)
        roomHobbyAdapter = RoomAdapter(requireContext(), this)
        roomCustomAdapter = RoomAdapter(requireContext(), this)

        getRegionRoom()
        getHobbyRoom()
        getCustomRoom()

        //Onclick
        btnNewRoom.setOnClickListener {
            val intent = Intent(activity, NewRoomActivity::class.java)
            startActivity(intent)
        }

        return v
    }

    private fun getRegionRoom() {
        databaseReference.child("users").child(auth.currentUser!!.uid).child("rooms").get()
            .addOnSuccessListener { roomList ->

                val listOfRooms : MutableList<Room> = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child("rooms").child("place").child(room.value.toString()).get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                listOfRooms.add(it.getValue(Room::class.java)!!)

                                roomRegionAdapter.rooms = listOfRooms
                                rvRegionRoom.adapter = roomRegionAdapter

                            }
                        }
                }

            }


    }

    private fun getHobbyRoom() {
        databaseReference.child("users").child(auth.currentUser!!.uid).child("rooms").get()
            .addOnSuccessListener { roomList ->

                val listOfRooms : MutableList<Room> = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child("rooms").child("hobby").child(room.value.toString()).get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                listOfRooms.add(it.getValue(Room::class.java)!!)
                                roomHobbyAdapter.rooms = listOfRooms
                                rvHobbyRoom.adapter = roomHobbyAdapter
                            }
                        }
                }
            }
    }

    private fun getCustomRoom() {
        databaseReference.child("users").child(auth.currentUser!!.uid).child("rooms").get()
            .addOnSuccessListener { roomList ->

                val listOfRooms : MutableList<Room> = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child("rooms").child("custom").child(room.value.toString()).get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                listOfRooms.add(it.getValue(Room::class.java)!!)
                                roomCustomAdapter.rooms = listOfRooms
                                rvCustomRoom.adapter = roomCustomAdapter
                            }
                        }
                }
            }
    }

    override fun onItemClickListener(roomName: String?) {
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra(ROOM_NAME, roomName)
        startActivity(intent)
    }
}