package com.example.printassistant;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import entity.Device_BT;
import utility.BluetoothConnector;
import utility.DeviceAdapter;
import utility.LoadingDialog;

import static utility.BluetoothConnector.mBluetoothAdapter;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST_CONSTANT = 1;

    private IntentFilter intentFilter;

    private ScanBluetoothReceiver scanBluetoothReceiver;

    private BottomSheetBehavior bottomSheetBehavior;

    private DeviceAdapter deviceAdapter;

    private RecyclerView recyclerView;

    private TextView tv_count;

    private ArrayList<Device_BT> devicesList_final = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_search = findViewById(R.id.searchBluetooth);
        tv_count = findViewById(R.id.tv_count);
        recyclerView = findViewById(R.id.device_list);

        Button button = findViewById(R.id.open);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetView();
//                LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
//                if(loadingDialog.isShowing())
//                    loadingDialog.dismiss();
//                else loadingDialog.show();
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                //获取运行时权限
                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CONSTANT);
                } else {
                    //开始扫描
                    BluetoothConnector.scanBluetooth(MainActivity.this);
                }

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deviceAdapter = new DeviceAdapter(devicesList_final);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(deviceAdapter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        scanBluetoothReceiver = new ScanBluetoothReceiver();
        registerReceiver(scanBluetoothReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(scanBluetoothReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    BluetoothConnector.scanBluetooth(MainActivity.this);
                } else {
                    Toast.makeText(this,"You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }
    }

    private void showBottomSheetView(){

//        if (bottomSheetBehavior == null) {
            //获取behavior
            View bottomSheetView = findViewById(R.id.device_list_container);
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setSkipCollapsed(true);
            //设置起始状态为隐藏，默认状态是折叠
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            //设置回调监听
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int i) {

                }

                @Override
                public void onSlide(@NonNull View view, float v) {

                }
            });
//        }
//        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//        } else {
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//        }

        Log.d("MainActivity", "showBottomSheetView: --success to show bottom sheet--");

    }

    class ScanBluetoothReceiver extends BroadcastReceiver{

        private String TAG = ScanBluetoothReceiver.class.getName();

        private ArrayList<Device_BT> devicesList_initial = new ArrayList<>();

        private Device_BT device_bt;

        LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this){
            @Override
            public void dismiss() {
                BluetoothConnector.cancelScanBluetooth();

                super.dismiss();
            }
        };


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Log.d(TAG, "onReceive: action-->>" + action);

            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d(TAG, "onReceive: 开始扫描");
                    loadingDialog.show();
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "onReceive: 结束扫描");
                    loadingDialog.dismiss();
                    if(devicesList_initial.size() == 0){
                        Toast.makeText(context,"扫描不到蓝牙设备",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //利用HashSet对初始列表中的蓝牙设备实例进行去重
                        HashSet<Device_BT> hashSet = new HashSet<>();
                        devicesList_final.clear();
                        for(Device_BT d: devicesList_initial){
                            //这里add方法返回值如果为false表示插入值已经存在，如果为true，则将该蓝牙设备实例放入最终扫描结果的列表中
                            Boolean check = hashSet.add(d);
                            if (check){
                                devicesList_final.add(d);
                                Log.d(TAG, "onReceive: +1<<<===");
                            }
                        }
                        Log.d(TAG, "onReceive: ！！！发现蓝牙设备"+devicesList_final.size()+"台");
                        //在列表中显示扫描到的蓝牙设备
                        tv_count.setText("发现蓝牙设备 "+devicesList_final.size()+" 台");
                        devicesList_initial.clear();
                        deviceAdapter.notifyDataSetChanged();
                        showBottomSheetView();


                    }

                    break;

                case BluetoothDevice.ACTION_FOUND:
                    Log.d(TAG, "onReceive: 发现设备");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device == null || device.getName() == null) return;

                    device_bt = new Device_BT();
                    device_bt.setDeviceName(device.getName());
                    device_bt.setDeviceAddress(device.getAddress());
                    //Get a BluetoothDevice object for the given Bluetooth hardware address.
                    device_bt.setDevice(mBluetoothAdapter.getRemoteDevice(device_bt.getDeviceAddress()));
                    devicesList_initial.add(device_bt);
                    Log.d(TAG, "onReceive: ===>>> DeviceName:"+device_bt.getDeviceName()+". Hardware Address:"+device_bt.getDeviceAddress());

                    break;

                default:
                    break;
            }

        }
    }
}
