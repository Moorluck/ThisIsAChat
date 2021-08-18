package be.bxl.moorluck.thisisachat.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.ChatActivity
import be.bxl.moorluck.thisisachat.R
import be.bxl.moorluck.thisisachat.adapters.PrivateAdapter
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

class PrivateFragment : Fragment(), RoomAdapter.ItemClickListener, PrivateAdapter.ItemClickListener {

    companion object {
        @JvmStatic
        fun newInstance() = PrivateFragment()
    }

    // Fire Base
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var storageReference: StorageReference

    //View
    private lateinit var rvRoom : RecyclerView
    private lateinit var etUserId : EditText
    private lateinit var btnAddUser : ImageButton

    //Adapter
    private lateinit var rvAdapter : PrivateAdapter

    //User
    private lateinit var user : User

    //ListOfProfileImg
    var listOfProfileImg : MutableList<String> = mutableListOf()
    var listOfRooms : MutableList<Room> = mutableListOf()
    var listOfNames : MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_private, container, false)

        // Firebase
        auth = Firebase.auth
        databaseReference = Firebase
            .database(FirebaseConst.URL_DATABASE)
            .reference
        storageReference = Firebase
            .storage(FirebaseConst.URL_STORAGE)
            .reference

        // View

        etUserId = view.findViewById(R.id.et_user_id_private_fragment)
        btnAddUser = view.findViewById(R.id.btn_add_user_private_fragment)

        rvRoom = view.findViewById(R.id.rv_private_fragment)
        rvRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvAdapter = PrivateAdapter(requireContext(), this)
        rvRoom.adapter = rvAdapter

        // OnClick

        btnAddUser.setOnClickListener {
            checkIfUserExist()
        }

        getUserInfo()
        getPrivateRoom()

        return view
    }



    private fun getUserInfo() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                user = it.getValue(User::class.java)!!
            }
    }

    private fun checkIfUserExist() {
        databaseReference.child(FirebaseConst.USERS).child(etUserId.text.toString()).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    checkIfPrivateRoomExist(auth.currentUser!!.uid, etUserId.text.toString())
                }
                else {
                    Toast.makeText(requireContext(), "Couldn't find the user", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkIfPrivateRoomExist(userId: String, otherUserId: String) {
        databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(userId + otherUserId).child(FirebaseConst.NAME)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    Toast.makeText(requireContext(), "You've already add this user", Toast.LENGTH_SHORT).show()
                }

                else {
                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(otherUserId + userId).child(FirebaseConst.NAME)
                        .get()
                        .addOnSuccessListener { room ->
                            if (room.exists()) {
                                Toast.makeText(requireContext(), "You've already add this user", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                createNewPrivateRoom(userId, otherUserId)
                            }
                        }
                }
            }
    }

    private fun createNewPrivateRoom(userId: String, otherUserId: String) {
        databaseReference.child(FirebaseConst.USERS).child(otherUserId).child(FirebaseConst.PSEUDO)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val room = Room(
                        type = FirebaseConst.PRIVATE,
                        id = userId + otherUserId,
                        name = userId + otherUserId,
                        users = mapOf(userId to user.pseudo!!, otherUserId to it.value.toString()),
                        photoRef = "",
                        grades = mapOf(Grade().name to Grade(users = mapOf(userId to user.pseudo!!, otherUserId to it.value.toString())))
                    )

                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(room.id!!).setValue(room)
                        .addOnSuccessListener {
                            addRoomToUser(room, otherUserId)
                        }
                }
            }

    }

    private fun addRoomToUser(room: Room, otherUserId: String) {
        databaseReference.child(FirebaseConst.USERS)
            .child(auth.currentUser!!.uid)
            .child(FirebaseConst.ROOMS)
            .child(UUID.randomUUID().toString())
            .setValue(room.id)
            .addOnSuccessListener {
                addRoomToOtherUser(room, otherUserId)
            }
    }

    private fun addRoomToOtherUser(room: Room, otherUserId: String) {
        databaseReference.child(FirebaseConst.USERS)
            .child(otherUserId)
            .child(FirebaseConst.ROOMS)
            .child(UUID.randomUUID().toString())
            .setValue(room.id)
            .addOnSuccessListener {
                getPrivateRoom()
            }
    }

    private fun getPrivateRoom() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(
            FirebaseConst.ROOMS).get()
            .addOnSuccessListener { roomList ->

                listOfRooms = mutableListOf()
                listOfProfileImg = mutableListOf()
                listOfNames = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(room.value.toString()).get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                val itemRoom = it.getValue(Room::class.java)!!

                                itemRoom.users.forEach { user ->
                                    if (user.key != auth.currentUser!!.uid) {
                                        getProfileImg(user.key, itemRoom)
                                    }
                                }
                            }
                        }
                }

            }


    }

    private fun getProfileImg(key: String, itemRoom: Room) {
        databaseReference.child(FirebaseConst.USERS).child(key).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val user = it.getValue(User::class.java)
                    listOfRooms.add(itemRoom)
                    listOfProfileImg.add(user!!.imgUrl!!)
                    listOfNames.add(user.pseudo.toString())

                    rvAdapter.names = listOfNames
                    rvAdapter.profileImgs = listOfProfileImg
                    rvAdapter.rooms = listOfRooms
                    rvRoom.adapter = rvAdapter
                }
            }
    }

    override fun onItemClickListener(roomName: String?, roomType: String?, roomId: String?) {
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra(ChatActivity.ROOM_NAME, roomName)
        intent.putExtra(ChatActivity.ROOM_TYPE, roomType)
        intent.putExtra(ChatActivity.ROOM_ID, roomId)
        startActivity(intent)
    }


}