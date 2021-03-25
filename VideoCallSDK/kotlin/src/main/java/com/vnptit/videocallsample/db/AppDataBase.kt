package com.vnptit.videocallsample.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vnptit.videocallsample.model.ContactCall
import java.util.concurrent.Executors

/**
 * Created by Ông Hoàng Nhật Phương on 1/26/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 */

@Database(entities = [ContactCall::class], version = 1, exportSchema = false)
abstract class AppDataBase() : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    lateinit var appDataBase : AppDataBase

    companion object {
        private var INSTANCE: AppDataBase? = null
        private const val NUMBER_OF_THREADS = 2

        val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        fun getInstance(context: Context): AppDataBase? {
            if (INSTANCE == null) {
                synchronized(AppDataBase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            AppDataBase::class.java, "videocall-database")
                            .addCallback(object : RoomDatabase.Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    databaseWriteExecutor.execute {
                                        val defaultContact = ContactCall("VNPT.IT Thiện release", "0xxxxxxxxx", "Androidf9e59fe340a13a78")
                                        val phuongContact = ContactCall("VNPT.IT Phương Dz", "0xxxxxxxxx", "Android133067cd2ec74807")
                                        val thienContact = ContactCall("VNPT.IT Thiện debug", "0xxxxxxxxx", "Android26dcd388e14abfa9")
                                        INSTANCE?.contactDao()?.insertAll(listOf(defaultContact,phuongContact,thienContact))
                                    }
                                }
                            })
                            .build()
                }
            }
            return INSTANCE
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }

}