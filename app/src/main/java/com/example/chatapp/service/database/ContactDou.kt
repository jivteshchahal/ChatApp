package com.example.chatapp.service.database

import androidx.room.*
import com.example.chatapp.service.model.ContactsModel

@Dao
interface ContactDou {
    @Query("SELECT * FROM CONTACTS ORDER BY name")
    fun loadAllContacts(): List<ContactsModel?>?

    @Insert
    fun insertContact(contactsModel: ContactsModel?)

    @Update
    fun updateContact(contactsModel: ContactsModel?)

    @Delete
    fun delete(contactsModel: ContactsModel?)

    @Query("SELECT * FROM CONTACTS WHERE name = :name")
    fun loadContactById(name: String): ContactsModel?
}