package com.example.chatapp.service.repositories

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.chatapp.service.model.ChatModel
import com.example.chatapp.view.adapters.ChatRecyclerViewAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.JsonObject
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class ChatListRepository {
    private var mAuth: FirebaseAuth? = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var TAG = "Chat Fragment"
    private var chatList: MutableList<ChatModel> = ArrayList()
    private lateinit var registration: ListenerRegistration
    private lateinit var mAdapter: ChatRecyclerViewAdapter
    private lateinit var userNumber: String
    private var mutableLiveDataList: MutableLiveData<List<ChatModel>> =
        MutableLiveData<List<ChatModel>>()

    companion object {
        private lateinit var instance: ChatListRepository
        fun getInstance(): ChatListRepository {
            instance = ChatListRepository()
            return instance
        }
    }

    fun oldMessages(otherUserNum: String, refresh: Boolean): MutableLiveData<List<ChatModel>> {
        var arrayListSize: Int
        userNumber = mAuth!!.currentUser!!.phoneNumber.toString()
        val docRef = db.collection("users").document(mAuth!!.currentUser!!.phoneNumber.toString())
            .collection("chat").document(otherUserNum)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    val arrayList = document.data!!["chat"] as java.util.ArrayList<*>?
                    if (arrayList!!.size != 0 && !refresh) {
                        arrayListSize = arrayList.size
                        if (arrayList.size < 21) {
                            chatList = setDataInList(
                                0,
                                arrayList.size - 1,
                                arrayList,
                                false
                            )
                            mutableLiveDataList.value = chatList
                        } else if (arrayList.size >= 21) {
                            chatList = setDataInList(
                                arrayList.size - 21,
                                arrayList.size - 1,
                                arrayList,
                                false
                            )
                            mutableLiveDataList.value = chatList
                        }
                    } else if (arrayList.size != 0 && refresh) {
                        arrayListSize = arrayList.size - 21
                        if (arrayListSize < 21) {
                            chatList = setDataInList(
                                0,
                                arrayListSize - 1,
                                arrayList,
                                true
                            )
                            mutableLiveDataList.postValue(chatList)
                        } else if (arrayList.size >= 21) {
                            chatList = setDataInList(
                                arrayListSize - 21,
                                arrayListSize - 1,
                                arrayList,
                                true
                            )
                            mutableLiveDataList.value = chatList
                        }
                    } else {
                        Log.d(TAG, "Current Array: null")
                    }
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        return mutableLiveDataList
    }

    fun messageReceived(otherUserNum: String): MutableLiveData<List<ChatModel>> {
        userNumber = mAuth!!.currentUser!!.phoneNumber.toString()
        val docRef = db.collection("users").document(userNumber)
            .collection("chat").document(otherUserNum)
        registration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val arrayList = snapshot.data!!["chat"] as java.util.ArrayList<*>?
                if (arrayList!!.size != 0) {
                    chatList = setDataInList(arrayList.size - 1, arrayList.size, arrayList, false)
                    mutableLiveDataList.value = chatList
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
//            updateToken(FirebaseInstanceId.getInstance().token)
        }
        return mutableLiveDataList
    }

    fun messageSent(
        otherUserNum: String,
        otherUserName: String,
        message: ChatModel
    ): MutableLiveData<List<ChatModel>> {
        val jsonObject = JsonObject()
        jsonObject.addProperty("chat_message", message.chatMessage)
        jsonObject.addProperty("chat_image", message.chatImage)
        jsonObject.addProperty("chat_video", message.chatVideo)
        jsonObject.addProperty("timestamp", message.timestamp.toString())
        jsonObject.addProperty("readBoolean", false)
        jsonObject.addProperty("sender", userNumber)
        val docData = hashMapOf(
            "chat" to FieldValue.arrayUnion(jsonObject.toString()), "name" to otherUserName
        )
        db.collection("users").document(userNumber)
            .collection("chat").document(otherUserNum)
            .update("chat", FieldValue.arrayUnion(jsonObject.toString()))
            .addOnSuccessListener {
                mutableLiveDataList.value = chatList
                Log.d(TAG, "DocumentSnapshot updated successfully!")
            }
            .addOnFailureListener {
                db.collection("users").document(userNumber)
                    .collection("chat").document(otherUserNum)
                    .set(docData)
                    .addOnSuccessListener {
                        mutableLiveDataList.value = chatList
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
        db.collection("users").document(otherUserNum)
            .collection("chat")
            .document(userNumber)
            .update("chat", FieldValue.arrayUnion(jsonObject.toString()))
            .addOnSuccessListener {
                mutableLiveDataList.value = chatList
                Log.d(TAG, "DocumentSnapshot updated successfully!")
            }
            .addOnFailureListener {
                db.collection("users").document(otherUserNum)
                    .collection("chat").document(userNumber)
                    .set(docData)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                        mutableLiveDataList.value = chatList
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
        return mutableLiveDataList
    }

    private fun setDataInList(
        start: Int,
        stop: Int,
        arrayList: ArrayList<*>, oldMsg: Boolean
    ): MutableList<ChatModel> {
        Log.e(TAG, arrayList.size.toString())
        for (i in start until stop) {
            val json = JSONObject(arrayList[i].toString())
            val userMsg = json.get("chat_message").toString()
            val sender = json.get("sender").toString()
            val time = json.get("timestamp").toString()
            val userImg = json.get("chat_image").toString().replace("\\", "")
            val userVideo = json.get("chat_video").toString()

            val user = if (sender == userNumber) {
                "one"
            } else {
                "two"
            }
            when {
                userMsg.isNotEmpty() -> {
                    if (oldMsg) {
                        chatList.add(0, ChatModel(userMsg, "", "", "", "", time, user))
                    } else {
                        chatList.add(ChatModel(userMsg, "", "", "", "", time, user))
                    }
                }
                userImg.isNotEmpty() -> {
                    if (oldMsg) {
                        chatList.add(0, ChatModel("", userImg, "", "", "", time, user))
                    } else {
                        chatList.add(ChatModel("", userImg, "", "", "", time, user))
                    }
                }
                userVideo.isNotEmpty() -> {
                    if (oldMsg) {
                        chatList.add(0, ChatModel("", "", "", "", userVideo, time, user))
                    } else {
                        chatList.add(ChatModel("", "", "", "", userVideo, time, user))
                    }
                }
            }
            Log.e(TAG, chatList.size.toString())

        }
        return chatList
    }

    fun saveChatPhoto(
        resultUri: Uri?,
        otherUserName: String,
        otherUserNum: String,
        context: Context,
        date: String
    ): MutableLiveData<List<ChatModel>> {
        userNumber = mAuth!!.currentUser!!.phoneNumber.toString()
        val randomString =
            "alaksaksjxn" + Math.random() + Math.random().toInt() + Math.random().toInt()
        if (resultUri != null) {
            val filepath: StorageReference =
                FirebaseStorage.getInstance().reference.child("chatImage").child(randomString)
            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, resultUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val baos = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 20, baos)
            filepath.putFile(resultUri).addOnSuccessListener {
                mutableLiveDataList.value =
                    uploadChatPhoto(randomString, otherUserName, otherUserNum, date)
            }.addOnFailureListener {

            }
        }
        return mutableLiveDataList
    }

    private fun uploadChatPhoto(
        reference: String,
        userName: String,
        userNum: String,
        date: String
    ): MutableList<ChatModel> {
        var imgUrl: String
        FirebaseStorage.getInstance().reference.child("chatImage")
            .child(reference).downloadUrl.addOnSuccessListener { uri ->
                val userProfile: MutableMap<String, Any> = HashMap()
                userProfile["image_url"] = uri.toString()
                imgUrl = uri.toString()
                chatList.add(ChatModel("", imgUrl, "", "", "", date, "one"))
//                chatList.add(ChatBean("", imgUrl, "", date, "one"))
                messageSent(userNum, userName, ChatModel("", imgUrl, "", "", "", date, "one"))
            }
        return chatList
    }
}