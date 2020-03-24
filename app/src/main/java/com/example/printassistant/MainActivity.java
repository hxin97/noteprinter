package com.example.printassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import com.example.printassistant.common.MyKey;
import com.example.printassistant.entity.Note_Info;
import com.example.printassistant.utility.BluetoothUtil;
import com.example.printassistant.utility.OnConnectListener;
import com.example.printassistant.utility.PrintUtil;
import com.githang.statusbar.StatusBarCompat;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSION_REQUEST_CONSTANT = 1;

    public static final int REQUEST_CODE = 11;

    public static final String RETURN_IF_UPDATE = "returnIfUpdate";

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    private ArrayList<Note_Info> noteInfoList;

    NoteListAdapter noteListAdapter;

    BottomPopupWindow bottomPopupWindow;

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private Switch switchSetBold, switchSetUnderline, switchSetTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置状态栏颜色
        StatusBarCompat.setStatusBarColor(this,getResources().getColor(R.color.default_color),true);
        toolbar = findViewById(R.id.toolbar_home);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        RecyclerView recyclerView = findViewById(R.id.note_info_list);
        switchSetBold = findViewById(R.id.switch_set_bold);
        switchSetUnderline = findViewById(R.id.switch_set_underline);
        switchSetTime = findViewById(R.id.switch_set_time);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.loading_circle);
        }

        initDrawerToggle();
        setListener();
        loadSharedPreferences();

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

    private void initDrawerToggle() {
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,
                R.string.drawer_open,R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void setListener() {
        switchSetBold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PrintUtil.BOLD_SETTING = true;
                } else {
                    PrintUtil.BOLD_SETTING = false;
                }
                setSharedPreferences();  //本地缓存写入设置
            }
        });
        switchSetUnderline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PrintUtil.UNDERLINE_SETTING = true;
                } else {
                    PrintUtil.UNDERLINE_SETTING = false;
                }
                setSharedPreferences();  //本地缓存写入设置
            }
        });
        switchSetTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PrintUtil.TIME_SETTING = true;
                } else {
                    PrintUtil.TIME_SETTING = false;
                }
                setSharedPreferences();  //本地缓存写入设置
            }
        });
    }

    private void loadSharedPreferences() {
        sharedPreferences = getSharedPreferences("styleSetting", MODE_PRIVATE);
        Boolean boldSetting = sharedPreferences.getBoolean("bold", false);
        Boolean underlineSetting = sharedPreferences.getBoolean("underline", false);
        Boolean timeSetting = sharedPreferences.getBoolean("time", false);
        PrintUtil.BOLD_SETTING = boldSetting;
        PrintUtil.UNDERLINE_SETTING = underlineSetting;
        PrintUtil.TIME_SETTING = timeSetting;
        if (boldSetting) switchSetBold.setChecked(true);
        if (underlineSetting) switchSetUnderline.setChecked(true);
        if (timeSetting) switchSetTime.setChecked(true);
    }

    private void setSharedPreferences () {
        sharedPreferences = getSharedPreferences("styleSetting", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean("bold", PrintUtil.BOLD_SETTING);
        editor.putBoolean("underline", PrintUtil.UNDERLINE_SETTING);
        editor.putBoolean("time", PrintUtil.TIME_SETTING);
        editor.apply();
    }

    private void loadNoteList(){
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bottomPopupWindow != null) {
            bottomPopupWindow.unRegisterScanReceiver();
            BluetoothUtil.getInstance().cancelScanBluetooth();
        }
        Log.d("MainActivity", "onDestroy: 已经成功注销监听器");
        BluetoothUtil.getInstance().onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (resultCode == RESULT_OK) {
                if (requestCode == MainActivity.REQUEST_CODE) {
                    int ifUpdate = data.getIntExtra(MainActivity.RETURN_IF_UPDATE, 0);
                    if (ifUpdate == 1) {
                        new UpdateListTask().execute();
                    }
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.device:
                if (bottomPopupWindow == null) {
                    bottomPopupWindow = new BottomPopupWindow(MainActivity.this);
                }
                bottomPopupWindow.showPopupWindow();
                break;
            default:
        }
        return true;
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

    //监听连接设备结果
//    private OnConnectListener onConnectListener = new OnConnectListener() {
//        @Override
//        public void returnResult(int taskCode) {
//            if(taskCode == MyKey.RESULT.COMMON_SUCCESS) {
//                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
//            } else if (taskCode == MyKey.RESULT.COMMON_FAIL) {
//                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
//            }
//        }
//    };

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


//    //监听搜索设备结果
//    class ScanBluetoothReceiver extends BroadcastReceiver{
//
//        private String TAG = ScanBluetoothReceiver.class.getName();
//
//        private ArrayList<Device_BT> devicesList_initial = new ArrayList<>();
//
//        private Device_BT device_bt;
//
//        LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this){
//            @Override
//            public void dismiss() {
//                BluetoothUtil.getInstance().cancelScanBluetooth();
//
//                super.dismiss();
//            }
//        };
//
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            //Log.INTERRUPTER(TAG, "onReceive: action-->>" + action);
//
//            if (action != null) {
//                switch (action) {
//                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
//                        Log.d(TAG, "onReceive: 开始扫描");
//                        loadingDialog.show();
//                        break;
//
//                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
//                        Log.d(TAG, "onReceive: 结束扫描");
//                        loadingDialog.dismiss();
//                        if(devicesList_initial.size() == 0){
//                            Toast.makeText(context,"扫描不到蓝牙设备",Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//                            //利用HashSet对初始列表中的蓝牙设备实例进行去重
//                            HashSet<Device_BT> hashSet = new HashSet<>();
//                            devicesList_final.clear();
//                            for(Device_BT d: devicesList_initial){
//                                //这里add方法返回值如果为false表示插入值已经存在，如果为true，则将该蓝牙设备实例放入最终扫描结果的列表中
//                                boolean check = hashSet.add(d);
//                                if (check){
//                                    devicesList_final.add(d);
//                                    Log.d(TAG, "onReceive: +1<<<===");
//                                }
//                            }
//                            Log.d(TAG, "onReceive: ！！！发现蓝牙设备"+devicesList_final.size()+"台");
//                            //在列表中显示扫描到的蓝牙设备
//                            tv_count.setText(String.format(getString(R.string.bottom_sheet_text),devicesList_final.size()));
//                            devicesList_initial.clear();
//                            deviceAdapter.notifyDataSetChanged();
//                            //showBottomSheetView();
//                            //注销广播接收器
//                            unregisterReceiver(scanBluetoothReceiver);
//                        }
//
//                        break;
//
//                    case BluetoothDevice.ACTION_FOUND:
//                        Log.d(TAG, "onReceive: 发现设备");
//                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                        if (device == null || device.getName() == null) return;
//
//                        device_bt = new Device_BT();
//                        device_bt.setDeviceName(device.getName());
//                        device_bt.setDeviceAddress(device.getAddress());
//                        //Get processTextContentPiece BluetoothDevice object for the given Bluetooth hardware address.
//                        device_bt.setDevice(mBluetoothAdapter.getRemoteDevice(device_bt.getDeviceAddress()));
//                        devicesList_initial.add(device_bt);
//                        Log.d(TAG, "onReceive: ===>>> DeviceName:"+device_bt.getDeviceName()+". Hardware Address:"+device_bt.getDeviceAddress());
//
//                        break;
//
//                    default:
//                        break;
//                }
//            }
//
//        }
//    }

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
