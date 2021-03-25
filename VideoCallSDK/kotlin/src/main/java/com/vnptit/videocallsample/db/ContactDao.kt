package com.vnptit.videocallsample.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vnptit.videocallsample.model.ContactCall

/**
 * Created by Ông Hoàng Nhật Phương on 1/26/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 */
@Dao
interface ContactDao {

    @Query("SELECT * FROM Contact")
    fun getAll(): LiveData<List<ContactCall>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertContact(contactCall: ContactCall)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(contactCall: List<ContactCall>)

    @Update
    fun update(contactCall: ContactCall)

    @Delete
    fun delete(contactCall: ContactCall)
}