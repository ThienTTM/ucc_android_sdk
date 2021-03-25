package com.vnptit.videocallsample.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.vnptit.video_call_sdk.cache.SharePref
import com.vnptit.video_call_sdk.call_handle_callback.CallHandlingObservable
import com.vnptit.video_call_sdk.call_handle_callback.CallHandlingObserver
import com.vnptit.video_call_sdk.model.calling.PersionReceiver
import com.vnptit.video_call_sdk.model.error.ErrorResult
import com.vnptit.video_call_sdk.model.request.RegisterDeviceParam
import com.vnptit.video_call_sdk.network.delegate.AppCallbackListener
import com.vnptit.video_call_sdk.network.delegate.SDKUtils
import com.vnptit.video_call_sdk.screen.OutgoingActivity
import com.vnptit.video_call_sdk.utils.AppCode
import com.vnptit.video_call_sdk.utils.Constants
import com.vnptit.video_call_sdk.utils.Constants.VIDEO_CALL_PREVIOUS_ACTIVITY
import com.vnptit.video_call_sdk.utils.FunctionUtils
import com.vnptit.video_call_sdk.utils.Utils
import com.vnptit.videocallsample.R
import com.vnptit.videocallsample.db.ContactRepository
import com.vnptit.videocallsample.home.adapter.HomeAdapter
import com.vnptit.videocallsample.model.ContactCall
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_contact_detail.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by Ông Hoàng Nhật Phương on 1/27/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 */
class HomeActivity : AppCompatActivity(), CallHandlingObserver {

    private lateinit var homeViewModel: MainViewModel

    private lateinit var contactRepository: ContactRepository

    private var mListContact: List<ContactCall> = emptyList()

    private lateinit var adapter: HomeAdapter

    private lateinit var detailDialog: AlertDialog

    private val mainViewModel : MainViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Đăng ký lắng nghe callback khi xử lý cuộc gọi
        CallHandlingObservable.getInstance().registerObserver(this)

        //Reset lại trạng thái cache ban đầu để nhận cuộc gọi
        SharePref.getInstance().put(Constants.SharedKey.VIDEO_CALL_IS_CALLING, false)
        initView()
        initObserve()
        handlEvent()
        handleErrorCall()
    }

    private fun handleErrorCall() {
        // Nhận lỗi Từ SDK
        val intent = this.intent
        val bundle = intent.extras
            val errorResult =  bundle?.getSerializable(Constants.ERROR_EXTRA)
        if(errorResult != null){
            showToast((errorResult as ErrorResult).errorCode +" " + errorResult.message)
        }
    }

    private fun initObserve() {
        //Đăng ký lắng nghe khi dữ liệu Contact có thay đổi
        val mPackageName = packageName
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegisterDeviceReceiver,
                IntentFilter(mPackageName + Constants.Action.CALL_REGISTER_DEVICE))

        //Kiểm tra và xin quyền cần thiết
        if (!Utils.hasPermissions(this, Manifest.permission.CAMERA)
                || !Utils.hasPermissions(this, Manifest.permission.RECORD_AUDIO)
                || !Utils.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }

        contactRepository = ContactRepository(this.application)
        mainViewModel.allContacts?.observe(this, { list ->
            Log.d("xxx", list.size.toString())
            mListContact = list
            if (list != null && list.isNotEmpty()) {
                llNoContact.visibility = View.GONE
                svListContact.visibility = View.VISIBLE
            } else {
                llNoContact.visibility = View.VISIBLE
                svListContact.visibility = View.GONE
            }
            adapter.replaceData(list)
        })


    }

    private fun initView() {
        val previousActivity = "com.vnptit.videocallsample.home.HomeActivity"
        rcvListContact.setHasFixedSize(true)
        rcvListContact.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        adapter = HomeAdapter(this, emptyList(), object : HomeAdapter.IOnClickListener {
            override fun onClickContact(entity: ContactCall, position: Int) {
                showDetailDialog(entity)
            }

            override fun onClickCall(entity: ContactCall, position: Int) {
                val intent = Intent(this@HomeActivity, OutgoingActivity::class.java)
                val bundle = Bundle()

                val persionReceiver = PersionReceiver()
                persionReceiver.receiverName = entity.name
                persionReceiver.phone = entity.phone
                val deviceIDs: MutableList<String> = ArrayList()
                entity.deviceID?.let { deviceIDs.add(it) }
                persionReceiver.deviceIdReceiver = deviceIDs
                //Thêm previousActivity thực hiện việc nhận Error lỗi
                bundle.putString(VIDEO_CALL_PREVIOUS_ACTIVITY, previousActivity)
                bundle.putSerializable(Constants.VIDEO_CALL_PERSION_RECEVIER_DATA, persionReceiver)
                intent.putExtras(bundle)
                startActivity(intent)
            }

        })

        rcvListContact.adapter = adapter

        edtID.setText(AppCode.DEVICE_ID)
        if (!FunctionUtils.isNetworkConnected(this)) {
            Toast.makeText(this, R.string.ekyc_vc_no_connection, Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun handlEvent() {
        btnRegister.setOnClickListener {
            processRegisterDevice()
        }

        btnAddContact.setOnClickListener {
            showDetailDialog(null)
        }
    }

    private fun showDetailDialog(contact: ContactCall?) {
        val builder = AlertDialog.Builder(this@HomeActivity)
        @SuppressLint("InflateParams") val viewInflated = LayoutInflater.from(this@HomeActivity).inflate(R.layout.dialog_contact_detail, null)
        if (contact == null) {
            viewInflated.btnXoa.visibility = View.GONE
        } else {
            viewInflated.btnXoa.visibility = View.VISIBLE
            viewInflated.txtTen.setText(contact.name)
            viewInflated.txtID.setText(contact.deviceID)
        }
        builder.setView(viewInflated)
        detailDialog = builder.show()
        viewInflated.ivThoat.setOnClickListener { detailDialog.dismiss() }
        viewInflated.btnThoat.setOnClickListener { detailDialog.dismiss() }
        viewInflated.btnLuu.setOnClickListener {
            if (contact != null) {
                        contact.name = viewInflated.txtTen.editableText.toString()
                        contact.phone = "0xxxxxxxxx"
                        contact.deviceID = viewInflated.txtID.editableText.toString()
                contactRepository.update(contact)
            } else {
                val newContact = ContactCall()
                newContact.name = viewInflated.txtTen.editableText.toString()
                newContact.phone = "0xxxxxxxxx"
                newContact.deviceID = viewInflated.txtID.editableText.toString()
                contactRepository.insert(newContact)
            }
            detailDialog.dismiss()
        }
        viewInflated.btnXoa.setOnClickListener {
            contact?.let { contactRepository.delete(it) }
            detailDialog.dismiss()
        }
    }


    private val mRegisterDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            processRegisterDevice()
        }

    }

    private fun processRegisterDevice() {
        val deviceToken = SharePref.getInstance().get(Constants.SharedKey.DEVICE_TOKEN, String::class.java)
        if (FunctionUtils.isNullOrEmpty(deviceToken)) {
            return
        }

        val listener = object : AppCallbackListener {
            override fun onError(p0: String?) {
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "Đăng ký thiết bị thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onSuccess(p0: String?) {
                runOnUiThread { Toast.makeText(this@HomeActivity, "Đăng ký thiết bị thành công!", Toast.LENGTH_SHORT).show() }
            }
        }

        val registerDeviceParam = RegisterDeviceParam()
        registerDeviceParam.deviceId = AppCode.DEVICE_ID
        registerDeviceParam.deviceToken = deviceToken
        registerDeviceParam.idgTokenId = AppCode.TOKEN_ID_SDK
        registerDeviceParam.access_token = AppCode.ACCESS_TOKEN_SDK
        registerDeviceParam.token_key = AppCode.TOKEN_KEY_SDK
        registerDeviceParam.token_id = AppCode.TOKEN_ID_SDK
        registerDeviceParam.topicUsing = AppCode.TOPIC_USING
        registerDeviceParam.personIdApp = AppCode.PERSON_ID //Fake customer id
        registerDeviceParam.personName = AppCode.PERSON_NAME

        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        val data = gson.toJson(registerDeviceParam)
        SDKUtils.registerDevice(this@HomeActivity, data, listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        //Hủy đăng ký lắng nghe callback khi xử lý cuộc gọi
        CallHandlingObservable.getInstance().removeObserver(this)
    }

    override fun onAcceptCall(p0: String?) {
        showToast("Callback: AcceptCall")
    }

    private fun showToast(s: String) {
        runOnUiThread { Toast.makeText(this@HomeActivity, s, Toast.LENGTH_LONG).show() }
    }

    override fun onRejectCall(p0: String?) {
        p0?.let { showToast(it) }
    }

    override fun onEndCall(p0: String?) {
        showToast(getString(R.string.end_call))
    }
}