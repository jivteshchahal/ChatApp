package com.example.chatapp.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.service.model.ChatModel
import com.example.chatapp.service.repositories.ChatListRepository

class ChatFragmentViewModel : ViewModel() {
    private lateinit var chatList: MutableLiveData<List<ChatModel>>
    private lateinit var chatListRepository: ChatListRepository
    fun init() {
        chatListRepository = ChatListRepository.getInstance()
    }

    fun getOldChat(otherUserNum: String, refresh: Boolean): LiveData<List<ChatModel>> {
        chatList = chatListRepository.oldMessages(otherUserNum, refresh)
        return chatList
    }

    fun dataIsLoading(): Boolean {
        var isLoading = true
        return isLoading
    }

    fun getMessageReceived(secondUserNum: String): LiveData<List<ChatModel>> {
        chatList = chatListRepository.messageReceived(secondUserNum)
        return chatList
    }

    fun getMessageSend(
        number: String,
        name: String,
        chatModel: ChatModel
    ): LiveData<List<ChatModel>> {
        chatList = chatListRepository.messageSent(number, name, chatModel)
        chatListRepository.messageSentNew(number, name, chatModel)
        return chatList
    }

    fun saveChatPhoto(
        uri: Uri,
        name: String,
        number: String,
        context: Context,
        date: String
    ): LiveData<List<ChatModel>> {
        chatList = chatListRepository.saveChatPhoto(uri, name, number, context, date)
        return chatList
    }
}