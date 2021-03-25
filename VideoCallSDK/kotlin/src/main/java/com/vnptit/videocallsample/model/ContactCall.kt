package com.vnptit.videocallsample.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by Ông Hoàng Nhật Phương on 1/26/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 */

@Entity(tableName = "Contact")
data class ContactCall(
        @ColumnInfo(name = "name")
        var name: String? = null,
        @ColumnInfo(name = "phone")
        var phone: String? = null,
        @ColumnInfo(name = "deviceID")
        var deviceID: String? = null) {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    constructor() : this("", "", "")

}