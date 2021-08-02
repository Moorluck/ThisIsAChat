package be.bxl.moorluck.thisisachat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import be.bxl.moorluck.thisisachat.fragments.RoomFragment

class ChatActivity : AppCompatActivity() {

    lateinit var tvTitle : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // View

        tvTitle = findViewById(R.id.tv_title_chat_activity)

        // Setup title

        val roomName = intent.getStringExtra(RoomFragment.ROOM_NAME)
        tvTitle.text = roomName
    }
}