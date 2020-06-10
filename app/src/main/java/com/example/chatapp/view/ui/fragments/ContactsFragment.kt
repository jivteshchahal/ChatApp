package com.example.chatapp.view.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.chatapp.R
import com.example.chatapp.service.model.ContactsModel
import com.example.chatapp.view.adapters.ContactsViewAdapter
import com.example.chatapp.viewModel.ContactsFragViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions


@Suppress(
    "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION", "UNREACHABLE_CODE",
    "UNCHECKED_CAST"
)
class ContactsFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename and change types of parameters
    private lateinit var recyclerView: RecyclerView
    private var mAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var db: FirebaseFirestore? = null
    private var contactsBoolean: Boolean = false
    private val CONTACTS_LOADER_ID = 1
    private val TAG = "ContactFragment"
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
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        val searchBar = view.findViewById<SearchView>(R.id.svContacts)
        val swipeContainer = view.findViewById<SwipeRefreshLayout>(R.id.sflContacts)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        val btnRefreshContacts = view.findViewById<ImageButton>(R.id.btnRefreshContacts)
        val searchLayout = view.findViewById<LinearLayout>(R.id.layoutSearch)
//        val mDb = AppDatabase.getInstance(activity!!.applicationContext)
        firebaseUser = mAuth?.currentUser
        recyclerView = view.findViewById(R.id.rcViewContacts)
        recyclerView.layoutManager = layoutManager
        swipeContainer.setOnRefreshListener {
            swipeContainer.isRefreshing = false
//            searchBar.visibility = View.VISIBLE
            searchLayout.visibility = View.VISIBLE
        }
        btnRefreshContacts.setOnClickListener {
            getPermission()
            getContacts()
//            recyclerView.adapter = ContactsViewAdapter(mDb!!.contactDou()!!.loadAllContacts() as List<ContactsModel>, activity)
        }
        getContacts()
        return view
    }

    private fun getContacts() {
        val contactsFragViewModel = ContactsFragViewModel()
        contactsFragViewModel.init()
        contactsFragViewModel.getContacts(activity!!).observe(activity!!, Observer {
            if (it.isNotEmpty() && activity != null) {
                recyclerView.adapter = ContactsViewAdapter(it, activity)
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {

//        Toast.makeText(
//            this,
//            "You cannot proceed without contacts permission",
//            Toast.LENGTH_LONG
//        ).show()

        // If request is cancelled, the result arrays are empty.

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LoaderManager.getInstance(this).initLoader(CONTACTS_LOADER_ID, null, this)
        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
//                        getContacts()

            Toast.makeText(
                activity,
                "You cannot proceed without contacts permission",
                Toast.LENGTH_LONG
            ).show()
//                        ContactsPermission(this).getPermission()
//                        FirebaseAuth.getInstance().signOut()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Add other 'when' lines to check for other
        // permissions this app might request.
    }


    private fun contactsLoader(): Loader<Cursor> {
        return CursorLoader(
            activity!!.applicationContext, ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
    }

    private fun contactsFromCursor(cur: Cursor?): List<ContactsModel>? {
        val contacts: MutableList<ContactsModel> = ArrayList()
        val cr = activity!!.contentResolver
        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (pCur!!.moveToNext()) {
                        var phoneNo = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                        phoneNo =
                            phoneNo.replace("\\s".toRegex(), "").replace("(", "").replace(")", "")
                                .replace("-", "")
                        val contact = ContactsModel(phoneNo, name, "")
                        contacts.add(contact)
                    }
                    pCur.close()
                }
            }
        }
//        cur?.close()
        return contacts
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (id == CONTACTS_LOADER_ID) {
            return contactsLoader()
        }
        return contactsLoader()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        val contacts = contactsFromCursor(data)
        val docData = hashMapOf("contacts" to contacts)
        db!!.collection("users").document(firebaseUser?.phoneNumber.toString())
            .collection("contacts")
            .document(
                firebaseUser?.phoneNumber.toString()
            )
            .set(docData, SetOptions.merge()).addOnSuccessListener {
                Log.e(TAG, "Contacts loaded Successfully")
                contactsBoolean = true
            }.addOnFailureListener {
                Log.e(TAG, "Contacts loaded UnSuccessfully")
            }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

    private fun getPermission() {
        val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1
        if (ContextCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.READ_CONTACTS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                // Should we show an explanation?

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity!!,
                        Manifest.permission.READ_CONTACTS
                    )
                ) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Toast.makeText(
                        activity,
                        "You cannot proceed without contacts permissionsss",
                        Toast.LENGTH_LONG
                    ).show()
//                    flag = false
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS
                    )

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
//                getPermission()
                }
            } else {
                // Permission has already been granted
//                flag = true
            }
        } else {
//            ContactsPermission(this).getPermission()
            LoaderManager.getInstance(this).initLoader(CONTACTS_LOADER_ID, null, this)
        }
    }
}