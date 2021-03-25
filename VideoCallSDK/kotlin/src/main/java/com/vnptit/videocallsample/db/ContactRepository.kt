package com.vnptit.videocallsample.db

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.vnptit.videocallsample.model.ContactCall
import java.util.concurrent.Executors

/**
 * Created by Ông Hoàng Nhật Phương on 1/26/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 */
class ContactRepository(context: Context) {
    private var contactDao: ContactDao? = AppDataBase.getInstance(context)?.contactDao()
    private var mAllContacts: LiveData<List<ContactCall>>?

    private val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

    init {
        mAllContacts = contactDao?.getAll()
    }


    fun getAllContacts(): LiveData<List<ContactCall>>? {
        return mAllContacts
    }

    fun insert(contactCall: ContactCall) {
        databaseWriteExecutor.execute { contactDao?.insertContact(contactCall) }
    }

    fun update(contactCall: ContactCall) {
        databaseWriteExecutor.execute { contactDao?.update(contactCall) }
    }

    fun delete(contactCall: ContactCall) {
        databaseWriteExecutor.execute { contactDao?.delete(contactCall) }
    }

    companion object {
        private const val NUMBER_OF_THREADS = 2
    }
}