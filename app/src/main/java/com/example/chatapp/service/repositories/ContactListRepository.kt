package com.example.chatapp.service.repositories

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatapp.service.helper.ContactsPermission
import com.example.chatapp.service.model.ContactsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class ContactListRepository {
    private var mAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var db: FirebaseFirestore? = null
    private var mutableLiveDataList: MutableLiveData<List<ContactsModel>> =
        MutableLiveData<List<ContactsModel>>()

    companion object {
        private lateinit var instance: ContactListRepository
        fun getInstance(): ContactListRepository {
            instance = ContactListRepository()
            return instance
        }
    }

    fun getVolleyData(activity: Activity): MutableLiveData<List<ContactsModel>> {
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        firebaseUser = mAuth?.currentUser

        val request = object : StringRequest(
            Method.POST,
            "https://us-central1-chat-app-68da4.cloudfunctions.net/regContacts/contacts",
            Response.Listener { response ->
                val gson = Gson()
                val listType = object : TypeToken<MutableList<ContactsModel>>() {}.type
                if (response.isNotEmpty()) {
                    val contactList: MutableList<ContactsModel> = ArrayList()
//                    val mDb = AppDatabase.getInstance(activity.applicationContext)
                    val jsonList = gson.fromJson<MutableList<ContactsModel>>(response, listType)
                    for (i in 0 until jsonList.size) {
                        db!!.collection("users").document(jsonList[i].phoneNumber)
                            .collection("profile").document(jsonList[i].phoneNumber).get()
                            .addOnSuccessListener { document1 ->
                                contactList.add(
                                    ContactsModel(
                                        jsonList[i].phoneNumber,
                                        jsonList[i].name,
                                        document1.data!!["image_url"].toString()
                                    )
                                )
//                                AppExecutors.instance!!.diskIO().execute(Runnable {
//                                    mDb!!.contactDou()!!.insertContact(
//                                        ContactsModel(
//                                            jsonList[i].phoneNumber,
//                                            jsonList[i].name,
//                                            document1.data!!["image_url"].toString()
//                                        )
//                                    )
//                                })
                                mutableLiveDataList.value = contactList
                            }
                            .addOnFailureListener { exception ->
                                Log.e(VolleyLog.TAG, "Error getting documents: ", exception)
                            }
                    }
                } else {
                    Toast.makeText(activity, "Contacts Access needed", Toast.LENGTH_SHORT).show()
                    ContactsPermission(activity).getPermission()
                }
            },
            Response.ErrorListener {
                Log.e("Error", "It did not work ${it.localizedMessage}")
            }
        ) {
            override fun getBody(): ByteArray {
                super.getBody()
                val params2 = HashMap<String, String>()
                params2["number"] = firebaseUser?.phoneNumber.toString()
                return JSONObject(params2 as HashMap<*, *>).toString().toByteArray()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["Content-Type"] = "application/json"
                return params
            }

            override fun getBodyContentType(): String {
                return "x-www-form-urlencoded"
            }

        }
        val requestQueue = Volley.newRequestQueue(activity)
        requestQueue.add(request)
        return mutableLiveDataList
    }
}