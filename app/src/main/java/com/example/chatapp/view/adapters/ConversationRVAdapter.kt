package com.example.chatapp.view.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.service.model.ConversationModel
import com.example.chatapp.view.ui.activities.ChatActivity
import com.example.chatapp.viewModel.ChatFragmentViewModel
import kotlinx.android.synthetic.main.fragment_item.view.*

class ConversationRVAdapter(
    private val mValues: List<ConversationModel>, private val activity: FragmentActivity?
) : RecyclerView.Adapter<ConversationRVAdapter.ViewHolder>() {
    private var unSeenCount: Int = -1
    private lateinit var chatFragmentViewModel: ChatFragmentViewModel
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_item, parent, false)
        chatFragmentViewModel = ChatFragmentViewModel()
        chatFragmentViewModel.init()
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversationModel = mValues[position]
        holder.tvName.text = conversationModel.name
        holder.tvLastMsgTime.visibility = View.VISIBLE
        holder.lastChat.visibility = View.VISIBLE
        chatFragmentViewModel.getMessageReceived(conversationModel.phoneNumber).observe(activity!!,
            Observer {
                unSeenCount += 1
                when {
                    it[it.size - 1].chatMessage != "" -> {
                        holder.lastChat.text = it[it.size - 1].chatMessage
                    }
                    it[it.size - 1].chatImage != "" -> {
                        holder.lastChat.text = "image"
                    }
                    it[it.size - 1].chatVideo != "" -> {
                        holder.lastChat.text = "video"
                    }
                    it[it.size - 1].chatVoiceNote != "" -> {
                        holder.lastChat.text = "voice note"
                    }
                    it[it.size - 1].chatLatLong != "" -> {
                        holder.lastChat.text = "location"
                    }
                }
                holder.tvLastMsgTime.text = it[it.size - 1].timestamp
                if (unSeenCount != 0) {
                    holder.tvUnSeenCount.visibility = View.VISIBLE
                    holder.tvUnSeenCount.text = unSeenCount.toString()
                }
            })

        if (conversationModel.img_url.isNotEmpty()) {
            Glide.with(activity).load(conversationModel.img_url).into(holder.imgProfile)
        } else {
            holder.imgProfile.visibility = View.INVISIBLE
        }
        holder.contactLayout.setOnClickListener {
            unSeenCount = -1
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra(
                activity.getString(R.string.intentChatOtherUserNum),
                conversationModel.phoneNumber
            )
            intent.putExtra(
                activity.getString(R.string.intentChatOtherUserName),
                conversationModel.name
            )
            activity.startActivity(intent)
//            }
        }
        with(holder.mView) {
            tag = conversationModel.phoneNumber
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val tvName: TextView = mView.tvNameCon
        val imgProfile: ImageView = mView.imgProfileCon
        val contactLayout: LinearLayoutCompat = mView.contactLayoutItem
        val lastChat: TextView = mView.tvChatCon
        val tvUnSeenCount: TextView = mView.tvChatConCount
        val tvLastMsgTime: TextView = mView.tvChatConTime
        override fun toString(): String {
            return super.toString() + " '" + tvName.text + "'"
        }
    }
}