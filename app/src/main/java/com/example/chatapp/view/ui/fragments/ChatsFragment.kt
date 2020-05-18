package com.example.chatapp.view.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.service.model.ChatModel
import com.example.chatapp.view.adapters.ChatRecyclerViewAdapter
import com.example.chatapp.viewModel.ChatFragmentViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNREACHABLE_CODE", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")
class ChatsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var TAG = "Chat Fragment"
    private lateinit var otherUserName: String
    private lateinit var otherUserNum: String
    private val db = FirebaseFirestore.getInstance()
    private lateinit var mAdapter: ChatRecyclerViewAdapter
    private lateinit var registration: ListenerRegistration
    private lateinit var date: String
    private lateinit var profileUrl: String
    private lateinit var profileName: String
    private lateinit var imageUri: Uri
    private lateinit var chatFragmentViewModel: ChatFragmentViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        otherUserNum = activity!!.intent.getStringExtra(getString(R.string.intentChatOtherUserNum))
        otherUserName =
            activity!!.intent.getStringExtra(getString(R.string.intentChatOtherUserName))
        chatFragmentViewModel = ChatFragmentViewModel()
        chatFragmentViewModel.init()
        val swipeContainer = view.findViewById<SwipeRefreshLayout>(R.id.swipeContainer)
        var refresh = false
        val hour = Calendar.getInstance()[Calendar.HOUR]
        val min = Calendar.getInstance()[Calendar.MINUTE]
        val pm = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        val noon: String
        noon = if (pm > 11) {
            " pm"
        } else {
            " am"
        }
//        checkNewMSG()
        date = "$hour:$min$noon"
        recyclerView = view.findViewById(R.id.rcViewChats)
        recyclerView.layoutManager = layoutManager
        val tvName = view.findViewById<TextView>(R.id.tvChatName)
        val tvLastSeen = view.findViewById<TextView>(R.id.tvLastSeen)
        val imgChat = view.findViewById<ImageView>(R.id.imgUserChat)
        val sendButton = view.findViewById<Button>(R.id.btnSend)
        val imgButtonSend = view.findViewById<ImageButton>(R.id.btnImageSend)
        val chatBox = view.findViewById<EditText>(R.id.edtChatBox)

        setChatUsrImageDetails(imgChat, tvName, tvLastSeen)
        chatFragmentViewModel.getOldChat(otherUserNum, false).observe(activity!!,
            Observer { chatListLive -> onLoadViewData(chatListLive, false) })
        chatFragmentViewModel.getMessageReceived(otherUserNum)
            .observe(activity!!,
                Observer { chatListLive -> onLoadViewData(chatListLive, false) })
        swipeContainer.setOnRefreshListener {
            chatFragmentViewModel.getOldChat(otherUserNum, true).observe(activity!!,
                Observer { chatListLive ->
                    refresh = true
                    onLoadViewData(chatListLive, refresh)
                    swipeContainer.isRefreshing = false
                })
        }
        chatBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (chatBox.text.toString() == "") {
                    sendButton.visibility = View.GONE
                    imgButtonSend.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                sendButton.visibility = View.GONE
                imgButtonSend.visibility = View.VISIBLE
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendButton.visibility = View.VISIBLE
                imgButtonSend.visibility = View.GONE
            }

        })
        if (chatBox.text.toString() != "") {
            sendButton.visibility = View.VISIBLE
            imgButtonSend.visibility = View.GONE
        } else {
            sendButton.visibility = View.GONE
            imgButtonSend.visibility = View.VISIBLE
        }
        //Setting Click on Camera Button
        imgButtonSend.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }
        //Setting Click on Send Button
        sendButton.setOnClickListener {
            if (chatBox.text.isNotEmpty()) {
//                chatList.add(ChatModel(chatBox.text.toString(), "", "", date, "one"))
//                mAdapter =
//                    ChatRecyclerViewAdapter(chatList, getString(R.string.flagChatSend), activity!!)
//                recyclerView.adapter = mAdapter
//                recyclerView.smoothScrollToPosition(mAdapter.itemCount - 1)
//                messageSent(ChatModel(chatBox.text.toString(), "", "", date, "one"))
                chatFragmentViewModel.getMessageSend(
                    otherUserNum,
                    otherUserName,
                    ChatModel(chatBox.text.toString(), "", "", "", "", date, "one")
                ).observe(activity!!,
                    Observer { chatListLive -> onLoadViewData(chatListLive, refresh) })
                chatBox.setText("")
            }
        }

        return view
    }

    private fun setChatUsrImageDetails(imgChat: ImageView, tvName: TextView, tvLastSeen: TextView) {

        val docRef = db.collection("users").document(otherUserNum)
            .collection("profile").document(otherUserNum)
        registration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                profileUrl = snapshot.data!!["image_url"].toString()
                profileName =
                    activity!!.intent.getStringExtra(getString(R.string.intentChatOtherUserName))
                tvName.text = profileName
                tvLastSeen.text = "Last Online: " + snapshot.data!!["lastSeen"].toString()
                Glide.with(activity!!).load(snapshot.data!!["image_url"]?.toString()).into(imgChat)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
        imgChat.setOnClickListener {
            val bundle = Bundle()
            if (profileUrl.isNotEmpty()) {
                bundle.putString("profile_img", profileUrl)
                bundle.putString("profile_name", profileName)
                val fragment = ChatDetailFragment()
                fragment.arguments = bundle
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.chat_container, fragment).commit()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data!!
            saveChatPhoto(imageUri)
        }
    }

    private fun saveChatPhoto(imageUri: Uri) {
        chatFragmentViewModel.saveChatPhoto(
            imageUri,
            otherUserName,
            otherUserNum,
            activity!!.applicationContext,
            date
        )
//            .observe(activity!!,
//            Observer { chatListLive -> onLoadViewData(chatListLive) })
    }

    private fun updateToken(refreshToken: String?) {
        val docRef = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)
            .collection("token").document(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)
//        val token = Token(refreshToken!!)
        val map = hashMapOf(
            "token" to refreshToken
        )
        docRef.set(map)
    }

    private fun checkNewMSG() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token
                // Log and toast
                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(TAG, msg)
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
            })
    }

    fun sendNotificationToUser(user: String?, message: String?) {
        val ref = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)
            .collection("notification")
            .document(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)

        val notification = hashMapOf<String, String>()
        notification["title"] = user!!
        notification["message"] = message!!
        ref.set(notification)
    }

    private fun onLoadViewData(chatListLive: List<ChatModel>?, refresh: Boolean) {
        var newList: MutableList<ChatModel> = ArrayList()
//            newList.addAll(0, chatListLive!!)
        mAdapter = ChatRecyclerViewAdapter(
            chatListLive, activity!!.getString(R.string.flagChatReceived),
            activity!!
        )
        Log.e("sdjhgcbjdsbcksdbcksjdc", chatListLive.toString())
        recyclerView.adapter = mAdapter
//            mAdapter.notifyItemInserted(0)
//            recyclerView.smoothScrollToPosition(mAdapter.itemCount - 1)
        mAdapter.notifyDataSetChanged()
        if (refresh) {

            recyclerView.smoothScrollToPosition(0)
        } else if (!refresh) {
            recyclerView.smoothScrollToPosition(mAdapter.itemCount - 1)
        }
    }
}