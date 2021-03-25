package com.vnptit.videocallsample

import android.app.Application
import android.provider.Settings
import androidx.multidex.MultiDexApplication
import com.vnptit.video_call_sdk.config.SDKConfig
import com.vnptit.video_call_sdk.utils.AppCode
import com.vnptit.video_call_sdk.utils.Constants
import com.vnptit.videocallsample.module.viewModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


/**
 * Created by Ông Hoàng Nhật Phương on 1/25/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 */
class MyApplication : MultiDexApplication() {

    private var mApplicationContext : Application? = null

    init {
        System.loadLibrary("native-lib")
    }

    private external fun getAccessToken(): String?

    private external fun getTokenID(): String?

    private external fun getTokenKey(): String?

    private external fun getClientID(): String?

    private external fun getClientSecret(): String?

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(viewModule)
        }

        mApplicationContext = this
        SDKConfig.setApplication(mApplicationContext)
        // truyền thông số cấu hình bộ key định danh vào SDK
        AppCode.TOKEN_ID_SDK = getTokenID()
        AppCode.TOKEN_KEY_SDK = getTokenKey()
        AppCode.CLIENT_ID = getClientID()
        AppCode.CLIENT_SECRET = getClientSecret()

        AppCode.DEVICE_ID = "Android" + Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        AppCode.PERSON_ID = "CustomerID" + Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) //Fake customer ID
        AppCode.PERSON_NAME = "Sample Name" //Customer name của app tích hợp

        AppCode.TOPIC_USING = AppCode.TOKEN_ID_SDK + "_" + AppCode.PERSON_ID + "_" + AppCode.DEVICE_ID
        AppCode.ENV = Constants.PRD;
        AppCode.isUsingSocket = true
    }
}