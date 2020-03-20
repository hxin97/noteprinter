package com.example.printassistant;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import com.example.printassistant.common.MyKey;
import com.example.printassistant.data.NoteContainer;
import com.example.printassistant.entity.Device_BT;
import com.example.printassistant.entity.Note;
import com.example.printassistant.entity.Note_Info;
import com.example.printassistant.task.TaskController;
import com.example.printassistant.utility.BluetoothUtil;
import com.example.printassistant.utility.DeviceAdapter;
import com.example.printassistant.utility.LoadingDialog;
import com.example.printassistant.utility.OnBluetoothListener;
import com.example.printassistant.utility.OnConnectListener;
import com.githang.statusbar.StatusBarCompat;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST_CONSTANT = 1;

    private IntentFilter intentFilter;

    private ScanBluetoothReceiver scanBluetoothReceiver;

    private DeviceAdapter deviceAdapter;

    private TextView tv_count;

    private ArrayList<Device_BT> devicesList_final;

    private BluetoothAdapter mBluetoothAdapter;

    private OnBluetoothListener onBluetoothListener;

    private ArrayList<Note_Info> noteInfoList;

    NoteListAdapter noteListAdapter;

    public static final int REQUEST_CODE = 11;
    public static final String RETURN_IF_UPDATE = "returnIfUpdate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置状态栏颜色
        StatusBarCompat.setStatusBarColor(this,getResources().getColor(R.color.default_color),true);

        Toolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        }

        RecyclerView recyclerView = findViewById(R.id.note_info_list);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        noteInfoList = new ArrayList<>();
        noteListAdapter = new NoteListAdapter(noteInfoList);
        recyclerView.setAdapter(noteListAdapter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadNoteList();
                Collections.sort(noteInfoList);
                noteListAdapter.notifyDataSetChanged();
            }
        }).start();


        //Button btn_search = findViewById(R.id.searchBluetooth);
        //tv_count = findViewById(R.id.tv_count);
        //RecyclerView recyclerView = findViewById(R.id.device_list);

        //Button button = findViewById(R.id.open);
        //button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showBottomSheetView();
//                LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
//                if(loadingDialog.isShowing())
//                    loadingDialog.dismiss();
//                else loadingDialog.show();
//            }
//        });

        //Button button2 = findViewById(R.id.printIt);
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                print();
//            }
//        });
//
//        btn_search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mBluetoothAdapter = BluetoothUtil.getInstance().getBluetoothAdapter();
//                //蓝牙任务初始化，设置监听器
//                TaskController.getTaskController().init();
//                //获取运行时权限
//                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CONSTANT);
//                } else {
//                    intentFilter = new IntentFilter();
//                    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//                    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//                    intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
//                    scanBluetoothReceiver = new ScanBluetoothReceiver();
//                    registerReceiver(scanBluetoothReceiver,intentFilter);
//                    //开始扫描
//                    BluetoothUtil.getInstance().scanBluetooth(MainActivity.this);
//                }
//
//            }
//        });
//
//        devicesList_final = new ArrayList<>();
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        deviceAdapter = new DeviceAdapter(devicesList_final, onConnectListener);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setAdapter(deviceAdapter);


    }

    public void loadNoteList(){
        noteInfoList.clear();
        for (String name:fileList()) {
            if (matchFileName(name)) {
                FileInputStream in = null;
                BufferedReader reader = null;
                StringBuilder content = new StringBuilder();
                try{
                    in = openFileInput(name);
                    reader = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    if((line = reader.readLine()) != null)
                        content.append(line.trim());
                    Note_Info noteInfo = new Note_Info();
                    noteInfo.setCreateTime(name);
                    noteInfo.setTextContent(content.toString());
                    noteInfoList.add(noteInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public boolean matchFileName(String fileName) {
        return Pattern.matches("^\\d*",fileName);
    }

    private void print() {
        String strText = "测试打印文字彻骨的寒冷已然使血液丧失了原有的温度。身体的每一部分，甚至连自己的神经也无法感受到身体的其余部位。\n" +
                "作为乌萨斯帝国皇帝直属内卫队Sigma小队的指挥，亚雷苏上校或许在他的生命中第一次感受到了一丝令人无法呼吸的压迫感。\n" +
                "不过这种感觉对他来说倒是带点新鲜感，毕竟无数的叛军、刺客、敌国士兵、甚至绰号“卡西米尔银枪”的骑士都未曾给过他这种感觉。";
        Log.d("MainActivity", "print: here to check strText.length() = " + strText.length());
        //打印文字
        if(strText != null && strText.length() > 0) {
            Note note = new Note(MyKey.PRINT_TYPE.TEXT, strText);
            note.setBold(false);
            note.setUnderline(false);
            List<Note> list = new ArrayList<>();
            list.add(note);
            NoteContainer noteContainer = new NoteContainer(-1L, "", list);
            TaskController.getTaskController().startPrintTask(noteContainer);
        } else {
            Toast.makeText(MainActivity.this, "！请输入内容", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销广播监听器

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MainActivity.REQUEST_CODE) {
                int ifUpdate = data.getIntExtra(MainActivity.RETURN_IF_UPDATE, 0);
                if (ifUpdate == 1) {
                    new UpdateListTask().execute();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    BluetoothUtil.getInstance().scanBluetooth(MainActivity.this);
                } else {
                    Toast.makeText(this,"You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }
    }


//    private void showBottomSheetView(){
//
//        //获取behavior
//        View bottomSheetView = findViewById(R.id.device_list_container);
//        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
//        bottomSheetBehavior.setHideable(true);
//        bottomSheetBehavior.setSkipCollapsed(true);
//        //设置起始状态为隐藏，默认状态是折叠
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//        //设置回调监听
//        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View view, int i) {
//
//            }
//
//            @Override
//            public void onSlide(@NonNull View view, float v) {
//
//            }
//        });
//
//        Log.d("MainActivity", "showBottomSheetView: --success to show bottom sheet--");
//
//    }

    //监听连接设备结果
    private OnConnectListener onConnectListener = new OnConnectListener() {
        @Override
        public void returnResult(int taskCode) {
            if(taskCode == MyKey.RESULT.COMMON_SUCCESS) {
                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            } else if (taskCode == MyKey.RESULT.COMMON_FAIL) {
                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //监听打印结果
    private OnConnectListener onPrintListener = new OnConnectListener() {
        @Override
        public void returnResult(int taskCode) {
            if (taskCode == MyKey.RESULT.COMMON_SUCCESS) {
                Toast.makeText(MainActivity.this, "打印成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "打印过程出现问题了", Toast.LENGTH_SHORT).show();
            }
        }
    };


    //监听搜索设备结果
    class ScanBluetoothReceiver extends BroadcastReceiver{

        private String TAG = ScanBluetoothReceiver.class.getName();

        private ArrayList<Device_BT> devicesList_initial = new ArrayList<>();

        private Device_BT device_bt;

        LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this){
            @Override
            public void dismiss() {
                BluetoothUtil.getInstance().cancelScanBluetooth();

                super.dismiss();
            }
        };


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Log.INTERRUPTER(TAG, "onReceive: action-->>" + action);

            if (action != null) {
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
                                boolean check = hashSet.add(d);
                                if (check){
                                    devicesList_final.add(d);
                                    Log.d(TAG, "onReceive: +1<<<===");
                                }
                            }
                            Log.d(TAG, "onReceive: ！！！发现蓝牙设备"+devicesList_final.size()+"台");
                            //在列表中显示扫描到的蓝牙设备
                            tv_count.setText(String.format(getString(R.string.bottom_sheet_text),devicesList_final.size()));
                            devicesList_initial.clear();
                            deviceAdapter.notifyDataSetChanged();
                            //showBottomSheetView();
                            //注销广播接收器
                            unregisterReceiver(scanBluetoothReceiver);
                        }

                        break;

                    case BluetoothDevice.ACTION_FOUND:
                        Log.d(TAG, "onReceive: 发现设备");
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device == null || device.getName() == null) return;

                        device_bt = new Device_BT();
                        device_bt.setDeviceName(device.getName());
                        device_bt.setDeviceAddress(device.getAddress());
                        //Get processTextContentPiece BluetoothDevice object for the given Bluetooth hardware address.
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

    private class UpdateListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            loadNoteList();
            Collections.sort(noteInfoList);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            noteListAdapter.notifyDataSetChanged();
        }
    }
}
