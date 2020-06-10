package com.example.chatapp.service.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactsModel(
    @PrimaryKey val phoneNumber: String,
    @ColumnInfo var name: String,
    @ColumnInfo var img_url: String
)
