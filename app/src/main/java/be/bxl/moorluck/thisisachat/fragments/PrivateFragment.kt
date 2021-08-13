package be.bxl.moorluck.thisisachat.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.ChatActivity
import be.bxl.moorluck.thisisachat.R
import be.bxl.moorluck.thisisachat.adapters.PrivateAdapter
import be.bxl.moorluck.thisisachat.adapters.RoomAdapter
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Room
import be.bxl.moorluck.thisisachat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

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

    //Adapter
    private lateinit var rvAdapter : PrivateAdapter

    //ListOfProfileImg
    var listOfProfileImg : MutableList<String> = mutableListOf()
    var listOfRooms : MutableList<Room> = mutableListOf()

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

        rvRoom = view.findViewById(R.id.rv_private_fragment)
        rvRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvAdapter = PrivateAdapter(requireContext(), this)
        rvRoom.adapter = rvAdapter

        getPrivateRoom()

        return view
    }

    private fun getPrivateRoom() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(
            FirebaseConst.ROOMS).get()
            .addOnSuccessListener { roomList ->

                listOfRooms = mutableListOf()
                listOfProfileImg = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(room.value.toString()).get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                val itemRoom = it.getValue(Room::class.java)!!

                                itemRoom.users.forEach { user ->
                                    Log.d("USERID", user.key)
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

                    rvAdapter.name = user.pseudo.toString()
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