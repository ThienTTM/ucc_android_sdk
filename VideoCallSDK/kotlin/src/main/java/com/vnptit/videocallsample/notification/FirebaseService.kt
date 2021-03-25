package com.vnptit.videocallsample.notification

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.GsonBuilder
import com.vnptit.video_call_sdk.cache.SharePref
import com.vnptit.video_call_sdk.model.calling.KeyCustomer
import com.vnptit.video_call_sdk.network.delegate.SDKUtils
import com.vnptit.video_call_sdk.utils.AppCode
import com.vnptit.video_call_sdk.utils.Constants
import com.vnptit.video_call_sdk.utils.FunctionUtils
import org.json.JSONObject


/**
 * Created by Ông Hoàng Nhật Phương on 1/27/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 */
class FirebaseService : FirebaseMessagingService() {

    private lateinit var mBroadcastManager: LocalBroadcastManager
    private var mPackageName: String? = null


    override fun onCreate() {
        super.onCreate()
        mBroadcastManager = LocalBroadcastManager.getInstance(this)
        mPackageName = applicationContext.packageName
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "NEW TOKEN: $token")
        SharePref.getInstance().put(Constants.SharedKey.DEVICE_TOKEN, token)
        registerDevice(token)
        super.onNewToken(token)
    }

    /**
     * Khi nhận message
     * @param remoteMessage
     * Khi nhận message đã có dữ liệu của NotificationMessage object rồi
     * Nhận từ Firebase bắn về và xử lý ở trong nền không phải bên trong app
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (!remoteMessage.data.isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            try {
                handleDataMessage(remoteMessage)
            } catch (e: Exception) {
                Log.d(TAG, "Exception: " + e.message)
            }
        }
    }

    private fun handleDataMessage(remoteMessage: RemoteMessage) {
        // convert payload sang jsonobject rồi sang string
        val messageData = JSONObject(remoteMessage.data as Map<*, *>).toString()

        /**
         * PENDING, ACCEPTED, REJECTED, FINISHED, TIMEOUT, DENIED, LEAVE
         * các dạng type (PENDING là đang calling)
         */
        val dataType = remoteMessage.data["title"]
        if (!FunctionUtils.isNullOrEmpty(messageData) && !FunctionUtils.isNullOrEmpty(dataType)) {
            if (dataType == Constants.CallStatus.PENDING) {
                // lấy calling trong cache
                val isCalling = SharePref.getInstance().get(Constants.SharedKey.VIDEO_CALL_IS_CALLING, Boolean::class.java)
                if (isCalling) { // đang call rồi thì return luôn
                    return
                }
                //schedule job to start call activity
                scheduleJob(messageData)
            } else {
                // bắn broadcast với action tương ứng, khi accept hay denied , xử lý trường hợp TIMEOUT
                val intent = Intent(mPackageName + Constants.Action.FIREBASE_CALL_MESSAGE)
                intent.putExtra(Constants.VIDEO_CALL_FIREBASE_MESSAGE_DATA, messageData)
                mBroadcastManager.sendBroadcast(intent)
            }
        }
    }

    /**
     * Schedule async work using WorkManager.
     * Lên lịch thực hiện nhất định start màn hình nhận cuộc gọi
     */
    private fun scheduleJob(messageData: String) {
        val previousActivity = "com.vnptit.videocallsample.home.HomeActivity"
        // đóng object chứa key để truyền vào SDK
        val keyCustomer = KeyCustomer()
        keyCustomer.access_token = AppCode.ACCESS_TOKEN_SDK
        keyCustomer.token_id = AppCode.TOKEN_ID_SDK
        keyCustomer.token_key = AppCode.TOKEN_KEY_SDK
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        val dataKeyCustomer = gson.toJson(keyCustomer)
        SDKUtils.callHandleNotification(this, messageData, previousActivity, dataKeyCustomer)
    }

    private fun registerDevice(deviceToken: String) {
        val intent = Intent(mPackageName + Constants.Action.CALL_REGISTER_DEVICE)
        intent.putExtra(Constants.VIDEO_CALL_REGISTER_DEVICE_DATA, deviceToken)
        mBroadcastManager.sendBroadcast(intent)
    }
    companion object {
        val TAG = "VC_MessagingService"
    }
}