package com.vnptit.videocallsample.notification;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vnptit.video_call_sdk.cache.SharePref;
import com.vnptit.video_call_sdk.model.calling.KeyCustomer;
import com.vnptit.video_call_sdk.network.delegate.SDKUtils;
import com.vnptit.video_call_sdk.utils.AppCode;
import com.vnptit.video_call_sdk.utils.FunctionUtils;

import org.json.JSONObject;

import static com.vnptit.video_call_sdk.utils.Constants.Action.CALL_REGISTER_DEVICE;
import static com.vnptit.video_call_sdk.utils.Constants.Action.FIREBASE_CALL_MESSAGE;
import static com.vnptit.video_call_sdk.utils.Constants.CallStatus.PENDING;
import static com.vnptit.video_call_sdk.utils.Constants.SharedKey.DEVICE_TOKEN;
import static com.vnptit.video_call_sdk.utils.Constants.SharedKey.VIDEO_CALL_IS_CALLING;
import static com.vnptit.video_call_sdk.utils.Constants.VIDEO_CALL_FIREBASE_MESSAGE_DATA;
import static com.vnptit.video_call_sdk.utils.Constants.VIDEO_CALL_REGISTER_DEVICE_DATA;

/**
 * Crb HVD 18/05/2020
 * Service firebase xử lý nhận notification
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = "VC_MyFirebaseMessagingService";
    private LocalBroadcastManager mBroadcastManager;
    private String mPackageName;

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mPackageName = getApplicationContext().getPackageName();
    }

    /**
     * Khi nhận message
     * @param remoteMessage
     * Khi nhận message đã có dữ liệu của NotificationMessage object rồi
     * Nhận từ Firebase bắn về và xử lý ở trong nền không phải bên trong app
     */
    @Override
    @SuppressLint("LongLogTag")
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // chú ý phải thêm super từ cha
        super.onMessageReceived(remoteMessage);
        Log.d(TAG,"From: " + remoteMessage.getFrom());

        if(!remoteMessage.getData().isEmpty()) {
            Log.d(TAG,"Message data payload: " + remoteMessage.getData());
            try {
                handleDataMessage(remoteMessage);
            } catch(Exception e) {
                Log.d(TAG,"Exception: " + e.getMessage());
            }
        }
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        // convert payload sang jsonobject rồi sang string
        String messageData = (new JSONObject(remoteMessage.getData())).toString();
        /**
         * PENDING, ACCEPTED, REJECTED, FINISHED, TIMEOUT, DENIED, LEAVE
         * các dạng type (PENDING là đang calling)
         */
        String dataType = remoteMessage.getData().get("title");
        if(!FunctionUtils.isNullOrEmpty(messageData) && !FunctionUtils.isNullOrEmpty(dataType)) {
            if(dataType.equals(PENDING)) {
                // lấy calling trong cache
                boolean isCalling = SharePref.getInstance().get(VIDEO_CALL_IS_CALLING, Boolean.class);
//                if(isCalling) { // đang call rồi thì return luôn
//                    return;
//                }
                //schedule job to start call activity
                scheduleJob(messageData);
            } else {
                // bắn broadcast với action tương ứng, khi accept hay denied , xử lý trường hợp TIMEOUT
                Intent intent = new Intent(mPackageName + FIREBASE_CALL_MESSAGE);
                intent.putExtra(VIDEO_CALL_FIREBASE_MESSAGE_DATA, messageData);
                mBroadcastManager.sendBroadcast(intent);
            }
        }
    }

    /**
     * Schedule async work using WorkManager.
     * Lên lịch thực hiện nhất định start màn hình nhận cuộc gọi
     */
    private void scheduleJob(String messageData) {
        String previousActivity = "com.vnptit.videocallsample.screen.HomeActivity";
        // đóng object chứa key để truyền vào SDK
        KeyCustomer keyCustomer = new KeyCustomer();
        keyCustomer.setAccess_token(AppCode.ACCESS_TOKEN_SDK);
        keyCustomer.setToken_id(AppCode.TOKEN_ID_SDK);
        keyCustomer.setToken_key(AppCode.TOKEN_KEY_SDK);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String dataKeyCustomer = gson.toJson(keyCustomer);
        SDKUtils.callHandleNotification(MyFirebaseMessagingService.this, messageData, previousActivity, dataKeyCustomer);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onNewToken(@NonNull String s) {
        Log.d(TAG,"NEW TOKEN: "+ s);
        SharePref.getInstance().put(DEVICE_TOKEN,s);
        registerDevice(s);
        super.onNewToken(s);
    }

    /**
     * Bắn broadcast ra ngoài MainActivity(lắng nghe) để thực hiện
     * Call service registerDevice khi có deviceToken
     */
    private void registerDevice(String deviceToken) {
        Intent intent = new Intent(mPackageName + CALL_REGISTER_DEVICE);
        intent.putExtra(VIDEO_CALL_REGISTER_DEVICE_DATA, deviceToken);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void getToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();
                    Log.d(TAG,"NEW TOKEN: "+ token);
                    SharePref.getInstance().put(DEVICE_TOKEN, token);
                    registerDevice(token);
                }
            });
    }

}
