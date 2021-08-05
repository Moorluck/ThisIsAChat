package be.bxl.moorluck.thisisachat.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.R
import be.bxl.moorluck.thisisachat.api.Url
import be.bxl.moorluck.thisisachat.consts.FirebaseConst
import be.bxl.moorluck.thisisachat.models.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class RoomAdapter(private val context : Context, private val itemClickListener : ItemClickListener) : RecyclerView.Adapter<RoomAdapter.ViewHolder>() {

    var rooms : List<Room> = listOf()

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val flRoom : FrameLayout = itemView.findViewById(R.id.fl_background_item_room)
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

        //TODO isn't working proprely
        if (room.messages.isNotEmpty()) {
            val lastMessage = room.messages.values.toMutableList()[0].pseudo.toString() +
                    " : " +
                    room.messages.values.toMutableList()[0].content.toString()
            holder.tvLastMessage.text = lastMessage
        }
        val numberUserMessage = "${room.users.size} / ${room.maxUsers}"
        holder.tvNumberOfUser.text = numberUserMessage

        // Setup the item room background
        val url = if (room.type == FirebaseConst.PLACE) {
            Url.getPhotoUrl(room.photoRef, 1000, 500, Url.getApiKey(context))
        }
        else {
            room.photoRef
        }

        Glide.with(context)
            .asDrawable()
            .load(url)
            .override(1500, 750)
            .centerCrop()
            .into(object : CustomTarget<Drawable>() {

                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    resource.alpha = 210
                    holder.flRoom.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClickListener(room.name, room.type)
        }

    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    interface ItemClickListener {
        fun onItemClickListener(roomName : String?, roomType : String?)
    }
}

