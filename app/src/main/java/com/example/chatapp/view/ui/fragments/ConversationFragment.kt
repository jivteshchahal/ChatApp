package com.example.chatapp.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.view.adapters.ConversationRVAdapter
import com.example.chatapp.viewModel.ConversationFragViewModel

@Suppress("IMPLICIT_CAST_TO_ANY")
class ConversationFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var conversationFragViewModel: ConversationFragViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_conversations, container, false)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.rcViewConversations)
        recyclerView.layoutManager = layoutManager
        conversationFragViewModel = ConversationFragViewModel()
        conversationFragViewModel.init(activity!!.applicationContext)

        conversationFragViewModel.getConversation().observe(activity!!, Observer {
            if (it.isNotEmpty() && activity != null) {
                val mAdapter: ConversationRVAdapter = ConversationRVAdapter(it, activity)
                recyclerView.adapter = mAdapter
                recyclerView.smoothScrollToPosition(mAdapter.itemCount - 1)
            }
        })
        return view
    }
}