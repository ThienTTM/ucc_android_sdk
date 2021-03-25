package com.vnptit.videocallsample.screen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vnptit.video_call_sdk.cache.SharePref;
import com.vnptit.video_call_sdk.call_handle_callback.CallHandlingObservable;
import com.vnptit.video_call_sdk.model.calling.PersionReceiver;
import com.vnptit.video_call_sdk.model.error.ErrorResult;
import com.vnptit.video_call_sdk.model.request.RegisterDeviceParam;
import com.vnptit.video_call_sdk.model.request.RemoveDeviceParam;
import com.vnptit.video_call_sdk.network.delegate.AppCallbackListener;
import com.vnptit.video_call_sdk.network.delegate.SDKUtils;
import com.vnptit.video_call_sdk.screen.OutgoingActivity;
import com.vnptit.video_call_sdk.utils.AppCode;
import com.vnptit.video_call_sdk.utils.Constants;
import com.vnptit.video_call_sdk.utils.FunctionUtils;
import com.vnptit.video_call_sdk.call_handle_callback.CallHandlingObserver;

import com.vnptit.video_call_sdk.utils.Utils;
import com.vnptit.videocallsample.R;
import com.vnptit.videocallsample.adapter.ContactAdapter;
import com.vnptit.videocallsample.model.Contact;
import com.vnptit.videocallsample.model.ContactViewModel;

import java.util.ArrayList;
import java.util.List;

import static com.vnptit.video_call_sdk.utils.Constants.Action.CALL_REGISTER_DEVICE;
import static com.vnptit.video_call_sdk.utils.Constants.SharedKey.DEVICE_TOKEN;
import static com.vnptit.video_call_sdk.utils.Constants.SharedKey.VIDEO_CALL_IS_CALLING;
import static com.vnptit.video_call_sdk.utils.Constants.VIDEO_CALL_PERSION_RECEVIER_DATA;
import static com.vnptit.video_call_sdk.utils.Constants.VIDEO_CALL_PREVIOUS_ACTIVITY;

public class HomeActivity extends AppCompatActivity implements CallHandlingObserver {

    private LinearLayout llNoContact;
    private ScrollView svListContact;
    private RecyclerView rcvListContact;
    private Button btnAddContact;
    private Button btnRegister;
    private Button btnRemove;
    private EditText edtID;
    private AlertDialog detailDialog;

    private List<Contact> mListContact;
    private ContactAdapter mContactAdapter;
    private ContactViewModel mContactViewModel;

    private static final String previousActivity = "com.vnptit.videocallsample.screen.HomeActivity";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Khởi tạo views
        initViews();

        //Đăng ký lắng nghe khi dữ liệu Contact có thay đổi
        mContactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        mContactViewModel.getAllContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(@Nullable final List<Contact> contacts) {
                if (contacts != null && contacts.size() > 0) {
                    llNoContact.setVisibility(View.GONE);
                    svListContact.setVisibility(View.VISIBLE);
                } else {
                    llNoContact.setVisibility(View.VISIBLE);
                    svListContact.setVisibility(View.GONE);
                }
                mContactAdapter.replaceData(contacts);
            }
        });

        if (!FunctionUtils.isNetworkConnected(this)) {
            Toast.makeText(this, R.string.ekyc_vc_no_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        AppCode.previousActivity = previousActivity;
        //Đăng ký lắng nghe callback khi xử lý cuộc gọi
        CallHandlingObservable.getInstance().registerObserver(this);

        //Reset lại trạng thái cache ban đầu để nhận cuộc gọi
        SharePref.getInstance().put(VIDEO_CALL_IS_CALLING, false);

        //Đăng ký lắng nghe sự kiện từ FireBase để gọi registerDevice
        String mPackageName = getPackageName();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegisterDeviceReceiver,
                new IntentFilter(mPackageName + CALL_REGISTER_DEVICE));

        //Kiểm tra và xin quyền cần thiết
        if (!Utils.hasPermissions(this, Manifest.permission.CAMERA)
        || !Utils.hasPermissions(this, Manifest.permission.RECORD_AUDIO)
        || !Utils.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        handleErrorCall();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Hủy đăng ký lắng nghe callback khi xử lý cuộc gọi
        CallHandlingObservable.getInstance().removeObserver(this);
    }

    @SuppressLint("SetTextI18n")
    private void initViews() {
        llNoContact = findViewById(R.id.llNoContact);
        svListContact = findViewById(R.id.svListContact);
        rcvListContact = findViewById(R.id.rcvListContact);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnRegister = findViewById(R.id.btnRegister);
        btnRemove = findViewById(R.id.btnRemoveDevice);
        edtID = findViewById(R.id.edtID);
        edtID.setText("Your ID: " + AppCode.PERSON_ID);

        rcvListContact.setHasFixedSize(true);
        rcvListContact.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mListContact = new ArrayList<>();
        mContactAdapter = new ContactAdapter(this, mListContact, onClickCall);
        rcvListContact.setAdapter(mContactAdapter);

        btnAddContact.setOnClickListener(v -> {
            showDetailDialog(null);
        });

        btnRegister.setOnClickListener(v -> {
            processRegisterDevice();
        });

        btnRemove.setOnClickListener(v -> {
            removeDevice();
        });
    }

    private ContactAdapter.IOnClickListener onClickCall = new ContactAdapter.IOnClickListener() {
        @Override
        public void onClickContact(Contact entity, int position) {
            showDetailDialog(entity);
        }

        @Override
        public void onClickCall(Contact entity, int position) {
            Intent intent = new Intent(HomeActivity.this, OutgoingActivity.class);
            Bundle bundle = new Bundle();

            if(entity != null) {
                PersionReceiver persionReceiver = new PersionReceiver();
                persionReceiver.setReceiverName(entity.getName());
                persionReceiver.setPhone(entity.getPhone());
                List<String> personIDs = new ArrayList<>();
                personIDs.add(entity.getPersonID());
                persionReceiver.setDeviceIdReceiver(personIDs);
                //Thêm previousActivity thực hiện việc nhận Error lỗi
                bundle.putString(VIDEO_CALL_PREVIOUS_ACTIVITY, previousActivity);
                bundle.putSerializable(VIDEO_CALL_PERSION_RECEVIER_DATA, persionReceiver);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtras(bundle);
            }
            startActivity(intent);
        }
    };


    private void handleErrorCall() {
        // Nhận lỗi Từ SDK
        Intent intent = this.getIntent();
        try {
            Bundle bundle = intent.getExtras();
            ErrorResult errorResult = (ErrorResult) bundle.getSerializable(Constants.ERROR_EXTRA);
            if(errorResult != null){
                showToast(errorResult.getErrorCode()+ " " + errorResult.getMessage());
            }
        }catch (Exception e){
            return;
        }
    }
    private void showDetailDialog(Contact contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        @SuppressLint("InflateParams") View viewInflated = LayoutInflater.from(HomeActivity.this).inflate(R.layout.dialog_contact_detail, null);
        ImageView ivThoat = viewInflated.findViewById(R.id.ivThoat);
        Button btnThoat = viewInflated.findViewById(R.id.btnThoat);
        Button btnLuu = viewInflated.findViewById(R.id.btnLuu);
        Button btnXoa = viewInflated.findViewById(R.id.btnXoa);
        EditText txtTen = viewInflated.findViewById(R.id.txtTen);
        EditText txtID = viewInflated.findViewById(R.id.txtID);

        if (contact == null) {
            btnXoa.setVisibility(View.GONE);
        } else {
            btnXoa.setVisibility(View.VISIBLE);
            txtTen.setText(contact.getName());
            txtID.setText(contact.getPersonID());
        }
        builder.setView(viewInflated);
        detailDialog = builder.show();

        ivThoat.setOnClickListener(v -> detailDialog.dismiss());
        btnThoat.setOnClickListener(v -> detailDialog.dismiss());
        btnLuu.setOnClickListener(v -> {
            boolean isUpdate = false;
            if (contact != null) {
                contact.setName(txtTen.getEditableText().toString());
                contact.setPhone("0xxxxxxxxx");
                contact.setPersonID(txtID.getEditableText().toString());
                mContactViewModel.update(contact);
            } else {
                Contact newContact = new Contact();
                newContact.setName(txtTen.getEditableText().toString());
                newContact.setPhone("0xxxxxxxxx");
                newContact.setPersonID(txtID.getEditableText().toString());
                mContactViewModel.insert(newContact);
            }
            detailDialog.dismiss();
        });
        btnXoa.setOnClickListener(v -> {
            mContactViewModel.delete(contact);
            detailDialog.dismiss();
        });
    }

    /**
     * Recevice Broadcast register device and call service.
     * Lắng nghe khi có sự thay đổi của deviceToken thì gọi xử lý cập nhật deviceToken
     */
    private BroadcastReceiver mRegisterDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            processRegisterDevice();
        }
    };

    private void processRegisterDevice() {
        String deviceToken = SharePref.getInstance().get(DEVICE_TOKEN, String.class);
        if (FunctionUtils.isNullOrEmpty(deviceToken)) {
            return;
        }

        AppCallbackListener listener = new AppCallbackListener() {
            @Override
            public void onError(String data) {
                HomeActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(HomeActivity.this, "Đăng ký thiết bị thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(String data) {
                HomeActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(HomeActivity.this, "Đăng ký thiết bị thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        RegisterDeviceParam registerDeviceParam = new RegisterDeviceParam();
        registerDeviceParam.setDeviceId(AppCode.DEVICE_ID);
        registerDeviceParam.setDeviceToken(deviceToken);
        registerDeviceParam.setIdgTokenId(AppCode.TOKEN_ID_SDK);
        registerDeviceParam.setAccess_token(AppCode.ACCESS_TOKEN_SDK);
        registerDeviceParam.setToken_key(AppCode.TOKEN_KEY_SDK);
        registerDeviceParam.setToken_id(AppCode.TOKEN_ID_SDK);
        registerDeviceParam.setTopicUsing(AppCode.TOPIC_USING);
        registerDeviceParam.setPersonIdApp(AppCode.PERSON_ID); //Fake customer id
        registerDeviceParam.setPersonName(AppCode.PERSON_NAME);

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String data = gson.toJson(registerDeviceParam);
        SDKUtils.registerDevice(HomeActivity.this, data, listener);
    }

    private void removeDevice() {

        String deviceToken = SharePref.getInstance().get(DEVICE_TOKEN, String.class);
        if (FunctionUtils.isNullOrEmpty(deviceToken)) {
            return;
        }

        AppCallbackListener listener = new AppCallbackListener() {
            @Override
            public void onError(String data) {
            }

            @Override
            public void onSuccess(String data) {
                HomeActivity.this.runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Remove thiết bị thành công!", Toast.LENGTH_SHORT).show());
            }
        };

        RemoveDeviceParam removeDeviceParam = new RemoveDeviceParam();
        removeDeviceParam.setDeviceId(AppCode.DEVICE_ID);
        removeDeviceParam.setIdgTokenId(AppCode.TOKEN_ID_SDK);
        removeDeviceParam.setPersonIdApp(AppCode.PERSON_ID); //Fake customer id
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String data = gson.toJson(removeDeviceParam);
        SDKUtils.removeDevice(HomeActivity.this, data, listener);
    }

    @Override
    public void onAcceptCall(String s) {
        Log.d("Home activity", s);
        showToast("Callback: AcceptCall");
    }

    @Override
    public void onRejectCall(String s) {
        Log.d("Home activity", s);
        showToast( getResources().getString(R.string.ekyc_vc_receiver_busy_call));
    }

    @Override
    public void onEndCall(String s) {
        Log.d("Home activity", s);
        showToast("Callback: EndCall");
    }

    private void showToast(String s) {
        HomeActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(HomeActivity.this, s ,Toast.LENGTH_LONG).show();
            }
        });
    }
}
