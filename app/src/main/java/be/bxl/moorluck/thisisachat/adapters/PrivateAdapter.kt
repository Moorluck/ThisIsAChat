package be.bxl.moorluck.thisisachat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.R
import be.bxl.moorluck.thisisachat.models.Room
import com.bumptech.glide.Glide

class PrivateAdapter(private val context : Context, private val itemClickListener : PrivateAdapter.ItemClickListener) : RecyclerView.Adapter<PrivateAdapter.ViewHolder>() {

    var rooms : List<Room> = listOf()
    var profileImgs : List<String> = listOf()

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile : ImageView = itemView.findViewById(R.id.img_profile_item_private)
        val tvPseudo : TextView = itemView.findViewById(R.id.tv_pseudo_item_private)
        val tvLastMessage : TextView = itemView.findViewById(R.id.tv_last_message_item_private)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivateAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val privateView = layoutInflater.inflate(R.layout.item_private, parent, false)

        return ViewHolder(privateView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = rooms[position]
        val imgProfile = profileImgs[position]

        holder.tvPseudo.text = room.name

        if (room.messages.isNotEmpty()) {
            val text = room.lastMessage!!.pseudo + " : " + room.lastMessage.content
            holder.tvLastMessage.text = text
        }

        Glide.with(context)
            .load(imgProfile)
            .into(holder.imgProfile)

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClickListener(room.name, room.type, room.id)
        }
    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    interface ItemClickListener {
        fun onItemClickListener(roomName : String?, roomType : String?, roomId : String?)
    }


}