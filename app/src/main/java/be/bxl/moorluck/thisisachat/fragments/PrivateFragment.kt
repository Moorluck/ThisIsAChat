package be.bxl.moorluck.thisisachat.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.ChatActivity
import be.bxl.moorluck.thisisachat.R
import be.bxl.moorluck.thisisachat.adapters.RoomAdapter
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class PrivateFragment : Fragment(), RoomAdapter.ItemClickListener {

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
    private lateinit var rvAdapter : RoomAdapter

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

        //TODO new adapter
        rvRoom = view.findViewById(R.id.rv_private_fragment)
        rvRoom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvAdapter = RoomAdapter(requireContext(), this)
        rvRoom.adapter = rvAdapter

        getPrivateRoom()

        return view
    }

    private fun getPrivateRoom() {
        databaseReference.child(FirebaseConst.USERS).child(auth.currentUser!!.uid).child(
            FirebaseConst.ROOMS).get()
            .addOnSuccessListener { roomList ->

                val listOfRooms : MutableList<Room> = mutableListOf()

                roomList.children.forEach { room ->
                    databaseReference.child(FirebaseConst.ROOMS).child(FirebaseConst.PRIVATE).child(room.value.toString()).get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                listOfRooms.add(it.getValue(Room::class.java)!!)

                                rvAdapter.rooms = listOfRooms
                                rvRoom.adapter = rvAdapter

                            }
                        }
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