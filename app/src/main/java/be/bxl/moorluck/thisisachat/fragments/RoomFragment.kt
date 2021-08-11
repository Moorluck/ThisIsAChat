package be.bxl.moorluck.thisisachat.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.ChatActivity
import be.bxl.moorluck.thisisachat.NewRoomActivity
import be.bxl.moorluck.thisisachat.R
import be.bxl.moorluck.thisisachat.adapters.RoomAdapter
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Grade
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

class RoomFragment : Fragment(), RoomAdapter.ItemClickListener {

    // Companion object

    companion object {
        @JvmStatic
        fun newInstance() = RoomFragment()
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
    lateinit var btnAddRoom : Button

    // Adapter

    private lateinit var roomRegionAdapter : RoomAdapter
    private lateinit var roomHobbyAdapter : RoomAdapter
    private lateinit var roomCustomAdapter : RoomAdapter

    // Room data

    var rooms : MutableList<Room> = mutableListOf()

    // User data

    lateinit var user : User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_room, container, false)

        // Firebase
        auth = Firebase.auth
        databaseReference = Firebase
            .database(FirebaseConst.URL_DATABASE)
            .reference
        storageReference = Firebase
            .storage(FirebaseConst.URL_STORAGE)
            .reference

        // View

        rvRegionRoom = v.findViewById(R.id.rv_region_room_fragment)
        rvHobbyRoom = v.findViewById(R.id.rv_hobby_room_fragment)
        rvCustomRoom = v.findViewById(R.id.rv_custom_room_fragment)

        btnNewRoom = v.findViewById(R.id.btn_new_room_room_fragment)
        btnAddRoom = v.findViewById(R.id.btn_join_new_room_room_fragment)

        // Set up recycler views
        rvRegionRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvHobbyRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvCustomRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        roomRegionAdapter = RoomAdapter(requireContext(), this)
        roomHobbyAdapter = RoomAdapter(requireContext(), this)
        roomCustomAdapter = RoomAdapter(requireContext(), this)


        //Onclick
        btnNewRoom.setOnClickListener {
            val intent = Intent(activity, NewRoomActivity::class.java)
            startActivity(intent)
        }

        btnAddRoom.setOnClickListener {
            val builder = AlertDialog.Builder(requireActivity())

            val dialogInflater = requireActivity().layoutInflater
            val view = dialogInflater.inflate(R.layout.dialog_join_room, null)

            val viewDialog = builder.setView(view)
                .setPositiveButton("Join") { dialog, _ ->
                    val etRoomId : EditText = view.findViewById(R.id.et_room_id_join_dialog)
                    addRoomToUser(etRoomId.text.toString())
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            viewDialog.show()
        }

        return v
    }



    override fun onResume() {
        super.onResume()

        getUserInfo()
        getRegionRoom()
        getHobbyRoom()
        getCustomRoom()
    }

    private fun getUserInfo() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                user = it.getValue(User::class.java)!!
            }
    }

    private fun getRegionRoom() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.ROOMS).get()
            .addOnSuccessListener { roomList ->

                val listOfRooms : MutableList<Room> = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PLACE).child(room.value.toString()).get()
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
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.ROOMS).get()
            .addOnSuccessListener { roomList ->

                val listOfRooms : MutableList<Room> = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.HOBBY).child(room.value.toString()).get()
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
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(FirebaseConst.ROOMS).get()
            .addOnSuccessListener { roomList ->

                val listOfRooms : MutableList<Room> = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.CUSTOM).child(room.value.toString()).get()
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

    private fun addRoomToUser(roomId: String) {

        databaseReference.child(FirebaseConst.ROOMS)
            .child(FirebaseConst.CUSTOM)
            .child(roomId)
            .child(FirebaseConst.USERS)
            .child(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    Toast.makeText(requireContext(), "You've already join this room", Toast.LENGTH_LONG).show()
                }

                else {
                    databaseReference.child(FirebaseConst.USERS)
                        .child(auth.currentUser!!.uid)
                        .child(FirebaseConst.ROOMS)
                        .child(UUID.randomUUID().toString())
                        .setValue(roomId)
                        .addOnSuccessListener {
                            joinARoom(roomId)
                        }
                }
            }


    }

    private fun joinARoom(roomId : String) {
        databaseReference.child(FirebaseConst.ROOMS)
            .child(FirebaseConst.CUSTOM)
            .child(roomId)
            .child(FirebaseConst.USERS)
            .child(auth.currentUser!!.uid)
            .setValue(user.pseudo)

        databaseReference.child(FirebaseConst.ROOMS)
            .child(FirebaseConst.CUSTOM)
            .child(roomId)
            .child(FirebaseConst.GRADES)
            .child(Grade().name)
            .child(FirebaseConst.USERS)
            .child(auth.currentUser!!.uid)
            .setValue(user.pseudo)
            .addOnSuccessListener {
                getCustomRoom()
            }
    }

    override fun onItemClickListener(roomName: String?, roomType : String?, roomId : String?) {
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra(ChatActivity.ROOM_NAME, roomName)
        intent.putExtra(ChatActivity.ROOM_TYPE, roomType)
        intent.putExtra(ChatActivity.ROOM_ID, roomId)
        startActivity(intent)
    }
}