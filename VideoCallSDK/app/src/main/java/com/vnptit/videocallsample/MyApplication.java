package com.vnptit.videocallsample;

import android.annotation.SuppressLint;
import android.app.Application;
import android.provider.Settings;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.vnptit.video_call_sdk.config.SDKConfig;
import com.vnptit.video_call_sdk.network.delegate.SDKUtils;
import com.vnptit.video_call_sdk.utils.AppCode;
import com.vnptit.video_call_sdk.utils.Constants;

public class MyApplication extends MultiDexApplication {
    private static String TAG = "MyApplication";
    private static Application mApplicationContext;

    static {
        System.loadLibrary("native-lib");
    }

    private native String getAccessToken();

    private native String getTokenID();

    private native String getTokenKey();

    private native String getClientID();

    private native String getClientSecret();

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {
        super.onCreate();

        // truyền Context vào trong SDK
        mApplicationContext = this;
        SDKConfig.setApplication(mApplicationContext);
        // truyền thông số cấu hình bộ key định danh vào SDK
        AppCode.TOKEN_ID_SDK = getTokenID();
        AppCode.TOKEN_KEY_SDK = getTokenKey();
        AppCode.CLIENT_ID = getClientID();
        AppCode.CLIENT_SECRET = getClientSecret();

        AppCode.DEVICE_ID = "Android" + Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        AppCode.PERSON_ID = "CustomerID" + Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID); //Fake customer ID
        AppCode.PERSON_NAME = "Sample Name"; //Customer name của app tích hợp
        AppCode.TOPIC_USING = AppCode.TOKEN_ID_SDK+"_" + AppCode.PERSON_ID + "_" + AppCode.DEVICE_ID;
        AppCode.ENV = Constants.PRD;
        AppCode.isUsingSocket = true;
    }
}
