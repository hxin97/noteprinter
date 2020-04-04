package com.example.noteprinter;

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

import com.example.noteprinter.common.MyKey;
import com.example.noteprinter.entity.Note_Info;
import com.example.noteprinter.task.TaskController;
import com.example.noteprinter.utility.BluetoothUtil;
import com.example.noteprinter.utility.OnConnectListener;
import com.example.noteprinter.utility.PrintUtil;
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
        TaskController.getTaskController().onDestroy();
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
