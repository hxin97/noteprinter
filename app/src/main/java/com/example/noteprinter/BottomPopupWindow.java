package com.example.noteprinter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noteprinter.common.MyKey;
import com.example.noteprinter.entity.Device_BT;
import com.example.noteprinter.task.TaskController;
import com.example.noteprinter.utility.BluetoothUtil;
import com.example.noteprinter.utility.OnConnectListener;

import java.util.ArrayList;

import static com.example.noteprinter.MainActivity.MY_PERMISSION_REQUEST_CONSTANT;

public class BottomPopupWindow {

    private static ArrayList<Device_BT> devicesList_final = new ArrayList<>();
    private static int buttonState = 1;
    private static final int BUTTON_STATE_DEFAULT = 1;
    private static final int BUTTON_STATE_SCANNING = 2;
    private static final int BUTTON_STATE_CONNECTING = 3;

    private DeviceAdapter deviceAdapter;
    public ScanBluetoothReceiver scanBluetoothReceiver;

    private BluetoothAdapter mBluetoothAdapter;
    private IntentFilter intentFilter;

    private TextView tv_count;
    private Context mContext;
    private PopupWindow popupWindow;
    private Button btnSearch;

    public BottomPopupWindow(final Context context) {
        this.mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_device, null);
        popupWindow = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(view);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setAnimationStyle(R.style.MyPopupWindowAnimStyle);

        tv_count = view.findViewById(R.id.tv_count);
        RecyclerView recyclerView = view.findViewById(R.id.device_list);
        //监听连接设备结果
        OnConnectListener onConnectListener = new OnConnectListener() {
            @Override
            public void returnResult(int taskCode) {
                if (taskCode == MyKey.RESULT.COMMON_SUCCESS) {
                    //Toast.makeText(mContext, "连接成功", Toast.LENGTH_SHORT).show();
                    for (Device_BT device: devicesList_final) {
                        if (device.getDeviceName().equals(BluetoothUtil.getInstance().getConnectDevice().getName())) {
                            device.setConnectState(true);
                            break;
                        }
                    }
                    btnSearch.setText(mContext.getString(R.string.popup_window_button_connecting));
                    buttonState = BUTTON_STATE_CONNECTING;
                    deviceAdapter.notifyDataSetChanged();
                } else if (taskCode == MyKey.RESULT.COMMON_FAIL) {
                    Toast.makeText(mContext, "连接失败", Toast.LENGTH_SHORT).show();
                    btnSearch.setText(mContext.getString(R.string.popup_window_button_default));
                    deviceAdapter.notifyDataSetChanged();
                } else if (taskCode == MyKey.RESULT.COMMON_LOST){
                    Toast.makeText(mContext, "断开连接", Toast.LENGTH_SHORT).show();
                    btnSearch.setText(mContext.getString(R.string.popup_window_button_default));
                    for (Device_BT device: devicesList_final) {
                        if (device.getConnectState()) device.setConnectState(false);
                    }
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        };
        deviceAdapter = new DeviceAdapter(devicesList_final, onConnectListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(deviceAdapter);

        if (devicesList_final.size() != 0) {
            tv_count.setText(String.format(mContext.getString(R.string.popup_window_text_found),devicesList_final.size()));
        }

        btnSearch = view.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter = BluetoothUtil.getInstance().getBluetoothAdapter();
                //当扫描正在进行中
                if (mBluetoothAdapter !=null && mBluetoothAdapter.isDiscovering()) {
                    BluetoothUtil.getInstance().cancelScanBluetooth();

                    unRegisterScanReceiver();

                    deviceAdapter.notifyDataSetChanged();
                    tv_count.setText(String.format(mContext.getString(R.string.popup_window_text_found),devicesList_final.size()));
                    btnSearch.setText(mContext.getString(R.string.popup_window_button_default));
                    buttonState = BUTTON_STATE_DEFAULT;
                    return;
                }
                //当设备连接中
                if (BluetoothUtil.getInstance().getState() == BluetoothUtil.STATE_CONNECT_SUCCEED) {
                    BluetoothUtil.getInstance().disConnect();
                    btnSearch.setText(mContext.getString(R.string.popup_window_button_default));
                    buttonState = BUTTON_STATE_DEFAULT;
                    deviceAdapter.notifyDataSetChanged();
                    return;
                }
                //蓝牙任务初始化，设置监听器
                TaskController.getTaskController().init();
                //获取运行时权限
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions((Activity)mContext,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CONSTANT);
                } else {
                    intentFilter = new IntentFilter();
                    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
                    scanBluetoothReceiver = new BottomPopupWindow.ScanBluetoothReceiver();
                    mContext.registerReceiver(scanBluetoothReceiver,intentFilter);
                    //开始扫描
                    BluetoothUtil.getInstance().scanBluetooth(mContext);
                }
            }
        });
    }

    public void unRegisterScanReceiver() {
        try{
            scanBluetoothReceiver.getCurrentContext().unregisterReceiver(scanBluetoothReceiver);
            BluetoothUtil.getInstance().cancelScanBluetooth();
            buttonState = BUTTON_STATE_DEFAULT;
        } catch (Exception e) {
            Log.d("BottomPopupWindow", "unRegisterScanReceiver: 已注销");
        }
    }

    public void showPopupWindow () {
        deviceAdapter.notifyDataSetChanged();
        switch (buttonState) {
            case BUTTON_STATE_DEFAULT:
                btnSearch.setText(mContext.getString(R.string.popup_window_button_default));
                break;
            case BUTTON_STATE_SCANNING:
                btnSearch.setText(mContext.getString(R.string.popup_window_button_scanning));
                break;
            case BUTTON_STATE_CONNECTING:
                btnSearch.setText(mContext.getString(R.string.popup_window_button_connecting));
                break;
            default:
                btnSearch.setText(mContext.getString(R.string.popup_window_button_default));
        }
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            tv_count.setText(mContext.getString(R.string.popup_window_text_scanning));
        }
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        setBackgroundAlpha(0.5f);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //隐藏底部popupWindow之后要把原Activity的透明度恢复
                setBackgroundAlpha(1.0f);
            }
        });
    }

    public void setBackgroundAlpha (float bgAlpha) {
        WindowManager.LayoutParams lp = ((Activity)mContext).getWindow().getAttributes();
        lp.alpha = bgAlpha;
        ((Activity)mContext).getWindow().setAttributes(lp);
    }

    //监听搜索设备结果
    class ScanBluetoothReceiver extends BroadcastReceiver {

        private String TAG = ScanBluetoothReceiver.class.getName();

        private Device_BT device_bt;

        private Context currentContext;

        public Context getCurrentContext () {
            return this.currentContext;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            this.currentContext = context;
            //Log.INTERRUPTER(TAG, "onReceive: action-->>" + action);

            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        devicesList_final.clear();
                        Log.d(TAG, "onReceive: 开始扫描");
                        tv_count.setText(context.getString(R.string.popup_window_text_scanning));
                        btnSearch.setText(context.getString(R.string.popup_window_button_scanning));
                        buttonState = BUTTON_STATE_SCANNING;
                        //loadingDialog.show();
                        break;

                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        Log.d(TAG, "onReceive: 结束扫描");
                        //loadingDialog.dismiss();
                        if(devicesList_final.size() == 0){
                            tv_count.setText(context.getString(R.string.popup_window_text_found_no_device));
                            deviceAdapter.notifyDataSetChanged();
                        }
                        else {
                            tv_count.setText(String.format(context.getString(R.string.popup_window_text_found),devicesList_final.size()));

                        }
                        //注销广播接收器
                        if (scanBluetoothReceiver != null) {
                            context.unregisterReceiver(scanBluetoothReceiver);
                        }
                        btnSearch.setText(context.getString(R.string.popup_window_button_default));
                        buttonState = BUTTON_STATE_DEFAULT;
                        break;

                    case BluetoothDevice.ACTION_FOUND:
                        Log.d(TAG, "onReceive: 发现设备");
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device == null || device.getName() == null) return;

                        device_bt = new Device_BT();
                        device_bt.setDeviceName(device.getName());
                        device_bt.setDeviceAddress(device.getAddress());
                        device_bt.setDeviceType(device.getBluetoothClass().getMajorDeviceClass());
                        //Get processTextContentPiece BluetoothDevice object for the given Bluetooth hardware address.
                        device_bt.setDevice(mBluetoothAdapter.getRemoteDevice(device_bt.getDeviceAddress()));

                        devicesList_final.add(device_bt);
                        deviceAdapter.notifyDataSetChanged();
                        Log.d(TAG, "onReceive: ===>>> DeviceName:"+device_bt.getDeviceName()+". Hardware Address:"+device_bt.getDeviceAddress());

                        break;

                    default:
                        break;
                }
            }

        }
    }
}
