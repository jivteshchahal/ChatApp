package com.example.chatapp.view.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.chatapp.R
import com.example.chatapp.view.adapters.ContactsViewAdapter
import com.example.chatapp.viewModel.ContactsFragViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION", "UNREACHABLE_CODE")
class ContactsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var recyclerView: RecyclerView
    private var mAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var db: FirebaseFirestore? = null
    private val CONTACTS_LOADER_ID = 1
    private var mFunctions: FirebaseFunctions? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val searchBar = view.findViewById<SearchView>(R.id.svContacts)
        val swipeContainer = view.findViewById<SwipeRefreshLayout>(R.id.sflContacts)
        swipeContainer.setOnRefreshListener {
            swipeContainer.isRefreshing = false
            searchBar.visibility = View.VISIBLE
        }
        firebaseUser = mAuth?.currentUser
        recyclerView = view.findViewById(R.id.rcViewContacts)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        getContacts()
        return view
    }

    private fun getContacts() {
        val contactsFragViewModel = ContactsFragViewModel()
        contactsFragViewModel.init()
        contactsFragViewModel.getContacts(activity!!).observe(activity!!, Observer {
            if(it.isNotEmpty()&& activity != null){
                recyclerView.adapter = ContactsViewAdapter(it, activity)
            }
        })
    }
}