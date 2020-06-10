package com.example.chatapp.service.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chatapp.service.model.ContactsModel

//@Database(entities = [ContactsModel::class], version = 1, exportSchema = false)
@Database(entities = [ContactsModel::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDou(): ContactDou?

    companion object {
        private val LOG_TAG = AppDatabase::class.java.simpleName
        private val LOCK = Any()
        private const val DATABASE_NAME = "contact_list"
        private var sInstance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    Log.d(
                        LOG_TAG,
                        "Creating new database instance"
                    )
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    ).build()
                }
            }
            Log.d(LOG_TAG, "Getting the database instance")
            return sInstance
        }
    }
}