package be.bxl.moorluck.thisisachat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.R
import be.bxl.moorluck.thisisachat.models.Room

class RoomAdapter : RecyclerView.Adapter<RoomAdapter.ViewHolder>() {

    var rooms : List<Room> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tvRoomName : TextView = itemView.findViewById(R.id.tv_room_name_item_room)
        val tvLastMessage : TextView = itemView.findViewById(R.id.tv_last_message_item_room)
        val tvNumberOfUser : TextView = itemView.findViewById(R.id.tv_number_member_item_room)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val roomView = layoutInflater.inflate(R.layout.item_room, parent, false)

        return ViewHolder(roomView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val room = rooms[position]

        holder.tvRoomName.text = room.name
        if (!room.message.isEmpty()) {
            holder.tvLastMessage.text = room.message.last().content
        }
        val numberUserMessage = "${room.users.size} / ${room.maxUsers}"
        holder.tvNumberOfUser.text = numberUserMessage

    }

    override fun getItemCount(): Int {
        return rooms.size
    }
}