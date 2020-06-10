package com.example.chatapp.view.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.R
import com.example.chatapp.service.helper.NetworkConnected
import com.example.chatapp.service.model.ProfileModel
import com.example.chatapp.viewModel.ProfileViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION", "NAME_SHADOWING")
class ProfileActivity : AppCompatActivity() {
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
//        getPermission()
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
//                if (contactsBoolean) {
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
//                } else {
//                    Snackbar.make(
//                        findViewById(android.R.id.content), "Please Wait Loading..",
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//
//                }

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


}