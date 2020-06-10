package com.example.chatapp.view.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.chatapp.R
import com.example.chatapp.view.adapters.TabAdapter
import com.example.chatapp.view.ui.fragments.ContactsFragment
import com.example.chatapp.view.ui.fragments.ConversationFragment
import com.example.chatapp.view.ui.fragments.SettingsFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        val viewPager = findViewById<ViewPager>(R.id.fragLayout)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val adapter = TabAdapter(supportFragmentManager)
        adapter.addFragment(ContactsFragment(), "Contacts")
        adapter.addFragment(ConversationFragment(), "Chats")
        adapter.addFragment(SettingsFragment(), "Settings")
        viewPager.adapter = adapter
//        tabLayout.selectT())
        tabLayout.setupWithViewPager(viewPager)
        val tab: TabLayout.Tab = tabLayout.getTabAt(1)!!
        tab.select()
        checkNewMSG()
//        bottomNavigation.st
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentLayout, ConversationFragment())
//            .commit()
//        bottomNavigation.isSelected = true
//        bottomNavigation.setOnNavigationItemSelectedListener {
//            when (it.itemId) {
//                R.id.btnChatFrag -> {
//                    // Respond to navigation item 1 click
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentLayout, ConversationFragment())
//                        .commit()
//                    true
//                }
//                R.id.btnCallFrag -> {
////                    // Respond to navigation item 2 click
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentLayout, CallsFragment())
//                        .commit()
//                    true
//                }
//                R.id.btnSettingsFrag -> {
////                    // Respond to navigation item 2 click
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentLayout, SettingsFragment())
//                        .commit()
//                    true
//                }
//                R.id.btnContactsFrag -> {
//                    // Respond to navigation item 2 click
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentLayout, ContactsFragment())
//                        .commit()
//                    true
//                }
//                else -> false
//            }
//        }
//        bottomNavigation.selectedItemId = R.id.btnChatFrag
//        bottomNavigation.setOnNavigationItemReselectedListener {
//            when (it.itemId) {
//                R.id.btnChatFrag -> {
//                    // Respond to navigation item 1 click
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentLayout, ConversationFragment())
//                        .commit()
//                }
//                R.id.btnCallFrag -> {
////                    // Respond to navigation item 2 click
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentLayout, CallsFragment())
//                        .commit()
//                }
//                R.id.btnSettingsFrag -> {
////                    // Respond to navigation item 2 click
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentLayout, SettingsFragment())
//                        .commit()
//                }
//                R.id.btnContactsFrag -> {
//                    // Respond to navigation item 2 click
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentLayout, ContactsFragment())
//                        .commit()
//                }
//                else -> false
//            }
//        }


    }

    private fun checkNewMSG() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("service failed", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token
                // Log and toast
                val msg = token
                updateToken(msg)
                Log.d("TAG", msg)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            })
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

}
