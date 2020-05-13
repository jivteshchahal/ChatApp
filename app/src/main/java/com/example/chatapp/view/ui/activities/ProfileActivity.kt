package com.example.chatapp.view.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.R
import com.example.chatapp.service.helper.ContactsPermission
import com.example.chatapp.service.helper.NetworkConnected
import com.example.chatapp.service.model.ContactsModel
import com.example.chatapp.service.model.ProfileModel
import com.example.chatapp.viewModel.ProfileViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Suppress("DEPRECATION", "NAME_SHADOWING")
class ProfileActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private var mAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "ProfileActivity"
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null
    private lateinit var edtName: EditText
    private lateinit var btnContinue: Button
    private var imgUrl: String? = ""
    private var contactsBoolean: Boolean = false
    private val CONTACTS_LOADER_ID = 1
    private lateinit var progressBar: ProgressBar
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initView()
        profileViewModel = ProfileViewModel()
        profileViewModel.init(applicationContext)
        mAuth = FirebaseAuth.getInstance()
        firebaseUser = mAuth!!.currentUser
        getPermission()
        setOldData(edtName, imageView)
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleSmall)
//        getOldData()
        profileViewModel.getOldData()
        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        btnContinue.setOnClickListener {
            if (NetworkConnected(application).isInternetConnected()) {
                if (contactsBoolean) {
                    if (edtName.text.toString().isNotBlank()) {
                        if (imgUrl != "" && firebaseUser != null) {

                            // Create a new user with a first and last name
                            // Create a new user with a first and last name
                            val user: MutableMap<String, Any> = HashMap()
                            user["name"] = edtName.text.toString()
                            user["lastSeen"] = "Not Yet"
                            val userId: MutableMap<String, Any> = HashMap()
                            userId["uid"] = firebaseUser!!.uid
                            db.collection(getString(R.string.dbKeyUser))
                                .document(firebaseUser!!.phoneNumber.toString()).set(userId)

                            db.collection(getString(R.string.dbKeyUser))
                                .document(firebaseUser!!.phoneNumber.toString())
                                .collection(getString(R.string.dbKeyProfile))
                                .document(firebaseUser!!.phoneNumber.toString()).update(user)
                                .addOnCompleteListener { task: Task<Void> ->
                                    if (task.isComplete) {
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                }
                                .addOnFailureListener {
                                    db.collection(getString(R.string.dbKeyUser))
                                        .document(firebaseUser!!.phoneNumber.toString())
                                        .collection(getString(R.string.dbKeyProfile))
                                        .document(firebaseUser!!.phoneNumber.toString())
                                        .set(user)
                                        .addOnCompleteListener { task: Task<Void> ->
                                            if (task.isComplete) {
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        MainActivity::class.java
                                                    )
                                                )
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                TAG,
                                                "Error adding document",
                                                e
                                            )
                                        }
                                }
                            val profileUpdates: UserProfileChangeRequest =
                                UserProfileChangeRequest.Builder()
                                    .setDisplayName(edtName.text.toString())
                                    .setPhotoUri(Uri.parse(imgUrl))
                                    .build()

                            firebaseUser!!.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "User profile updated.")
                                    }
                                }
                        } else {
                            Snackbar.make(
                                findViewById(android.R.id.content), "Profile Image not set",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content), "Name not set",
                            Snackbar.LENGTH_SHORT
                        ).show()

                    }
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content), "Please Wait Loading..",
                        Snackbar.LENGTH_SHORT
                    ).show()

                }

            } else {
                Snackbar.make(
                    findViewById(android.R.id.content), "Internet Not Connected",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initView() {
        btnContinue = findViewById(R.id.btnProfContinue)
        edtName = findViewById(R.id.edtProfName)
        imageView = findViewById(R.id.imgProfile)
    }

    @SuppressLint("CheckResult")
    private fun setOldData(edtName: EditText?, imageView: ImageView?) {

        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.load)
        requestOptions.error(R.drawable.ic_launcher_foreground)
        val profileData: LiveData<ProfileModel> = profileViewModel.getOldData()
        profileData.observe(this, Observer {
            edtName!!.setText(it.name)
            imgUrl = it.imageUrl
            Glide.with(this).load(it.imageUrl).apply(requestOptions).into(imageView!!)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            imageUri = data!!.data
//            saveUserPhoto(imageUri, imageView)
            profileViewModel.getImage(imageUri).observe(this,
                Observer {
                    imgUrl = it
                    Glide.with(this).load(imgUrl).into(imageView)
                })
        }
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
                this,
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
            applicationContext!!, ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
    }

    private fun contactsFromCursor(cur: Cursor?): List<ContactsModel>? {
        val contacts: MutableList<ContactsModel> = ArrayList()
        val cr = contentResolver
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
        db.collection("users").document(firebaseUser?.phoneNumber.toString())
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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ContactsPermission(this).getPermission()
        } else {
//            ContactsPermission(this).getPermission()
            LoaderManager.getInstance(this).initLoader(CONTACTS_LOADER_ID, null, this)
        }
    }

}