package be.bxl.moorluck.thisisachat.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import be.bxl.moorluck.thisisachat.R
import be.bxl.moorluck.thisisachat.api.Url
import be.bxl.moorluck.thisisachat.models.Message
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

class ChatAdapter(val context : Context, private val userId : String,
                  private val onLongClickListener : (userId : String, view : View) -> Unit) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    companion object {
        const val TYPE_TO = 1
        const val TYPE_FROM = 2
    }

    var messages : List<Message> = listOf()

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val tvChat : TextView = itemView.findViewById(R.id.tv_item_message)
        val imgContent : ImageView = itemView.findViewById(R.id.img_message_item_message)
        val imgProfile : ImageView = itemView.findViewById(R.id.img_profile_item_message)

    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].userId == userId) {
            TYPE_TO
        } else {
            TYPE_FROM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        when (viewType) {
            TYPE_TO -> {
                val chatView = layoutInflater.inflate(R.layout.item_message_to, parent, false)
                return ViewHolder(chatView)
            }

            TYPE_FROM -> {
                val chatView = layoutInflater.inflate(R.layout.item_message_from, parent, false)
                return ViewHolder(chatView)
            }

            else -> {
                val chatView = layoutInflater.inflate(R.layout.item_message_from, parent, false)
                return ViewHolder(chatView)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.setIsRecyclable(false)

        if (messages[position].content != null) {
            holder.tvChat.text = messages[position].content
        }

        if (messages[position].imgContent != null) {
            Glide.with(context)
                .asDrawable()
                .load(messages[position].imgContent)
                .fitCenter()
                .into(holder.imgContent)
        }


        if (position == 0 || (messages[position-1].userId != messages[position].userId)) {
            Glide.with(context)
                .asDrawable()
                .load(messages[position].imgProfileRef)
                .fitCenter()
                .centerCrop()
                .into(holder.imgProfile)
        }
        else {
            holder.imgProfile.layoutParams.height = 0
        }

        holder.imgProfile.setOnLongClickListener {
            if (messages[position].userId != null) {
                onLongClickListener(messages[position].userId!!, it)
                return@setOnLongClickListener true
            }
            else {
                return@setOnLongClickListener false
            }
        }

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}