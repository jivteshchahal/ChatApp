package com.example.chatapp.service.helper

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ContactsPermission(private val thisActivity: Activity) {
    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1
    fun getPermission(): Boolean {
        var flag: Boolean = false
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                thisActivity,
                Manifest.permission.READ_CONTACTS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Should we show an explanation?

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    thisActivity,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(
                    thisActivity,
                    "You cannot proceed without contacts permissions",
                    Toast.LENGTH_LONG
                ).show()
                flag = false
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    thisActivity,
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
            flag = true
        }
        return flag
    }

}