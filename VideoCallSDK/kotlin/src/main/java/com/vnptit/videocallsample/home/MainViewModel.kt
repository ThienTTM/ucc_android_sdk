package com.vnptit.videocallsample.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.vnptit.videocallsample.db.ContactRepository
import com.vnptit.videocallsample.model.ContactCall


/**
 * Created by Ông Hoàng Nhật Phương on 1/26/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 * Class viewmodel đang chưa xử dụng
 */

class MainViewModel(context: Context) : ViewModel() {
    private val mRepository: ContactRepository
    val allContacts: LiveData<List<ContactCall>>?

    fun insert(contactCall: ContactCall?) {
        mRepository.insert(contactCall!!)
    }

    fun update(contactCall: ContactCall?) {
        mRepository.update(contactCall!!)
    }

    fun delete(contactCall: ContactCall?) {
        mRepository.delete(contactCall!!)
    }

    init {
        mRepository = ContactRepository(context)
        allContacts = mRepository.getAllContacts()
    }
}

