package com.example.chatays.utils

import android.content.Context
import android.provider.ContactsContract
import android.util.Log

//rehberden emailleri çek
object ContactsHelper {
    fun getContactsEmails(context: Context): List<String> {
        val emails = mutableListOf<String>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            null, null, null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val email = it.getString(it.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Email.ADDRESS
                ))
                emails.add(email)
            }
        }
        Log.d("ContactsHelper", "Toplam rehber e-postası: ${emails.size}")
        emails.forEach { Log.d("ContactsHelper", "Email: $it") }

        return emails.distinct()
    }
}
