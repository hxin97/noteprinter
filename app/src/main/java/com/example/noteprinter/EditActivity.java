package com.example.noteprinter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noteprinter.common.MyKey;
import com.example.noteprinter.data.NoteContainer;
import com.example.noteprinter.entity.Note;
import com.example.noteprinter.task.TaskController;
import com.example.noteprinter.utility.BluetoothUtil;
import com.example.noteprinter.utility.OnConnectListener;
import com.example.noteprinter.utility.PrintUtil;
import com.githang.statusbar.StatusBarCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    EditText editText;
    TextView tv_createTime;
    SimpleDateFormat df1;
    DateFormat df2;
    String existFileName;
    String fileContent;
    BottomPopupWindow bottomPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        //设置状态栏颜色
        StatusBarCompat.setStatusBarColor(this,getResources().getColor(R.color.white),true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        editText = findViewById(R.id.editText);
        tv_createTime = findViewById(R.id.createTime);

        df1 = new SimpleDateFormat("yyMMddHHmmss",Locale.CHINA);
        df2 = DateFormat.getDateTimeInstance(DateFormat.YEAR_FIELD,DateFormat.DATE_FIELD,Locale.CHINA);

        Intent intent = getIntent();
        existFileName = intent.getStringExtra("fileName"); //传递文件名

        if (existFileName != null) {
            String createTime = transformTime(existFileName);
            tv_createTime.setText(createTime);
            load(existFileName);
        } else {
            String currentTime = df2.format(new Date());
            tv_createTime.setText(currentTime);
            //Log.d("EditActivity", "onCreate: here: "+currentTime);
        }

    }

    //加载文件内容
    public void load(String existFileName) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = openFileInput(existFileName);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line+"\n");
            }
            content.deleteCharAt(content.length()-1); //删去最后添加的回车键符号
            fileContent = (content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            editText.setText(fileContent); //加载进编辑框
            if (reader != null) {
                try {
                    reader.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //保存编辑好的便签，以保存时的时间转换为连续数字串作为文件名。
    //保存后判断是新建文件还是在已存在文件上修改，如果是后者，把旧文件删除。
    public int save() {

        String fileName = df1.format(new Date());
        String data = editText.getText().toString();
        //如果输入框为空，判断是打开了新建还是删除了原本内容。
        if (data.equals("")) {
            if (existFileName != null) {
                delete(existFileName);
                return 0;
            } else
                return -1;
        }
        //如果没有做出修改，则既不做保存，也不做删除。
        if (fileContent != null) {
            if (data.equals(fileContent)) {
                return -1;
            }
        }

        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if (writer != null) {
                    writer.close();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if (existFileName != null && (!existFileName.equals(fileName))) {
            deleteFile(existFileName);
        }
        existFileName = fileName;
        return 0;
    }

    public void delete(String existFileName) {
        deleteFile(existFileName);
    }

    private void print() {
        String strText = editText.getText().toString();
        Log.d("EditActivity", "print: here to check strText.length() = " + strText.length());

        //打印文字
        if (strText.length() > 0) {
            //添加页眉，创建纸条的时间
            if (PrintUtil.TIME_SETTING) {
                strText = tv_createTime.getText().toString() + "\n" + "\n" + strText;
            }
            //设置打印参数
            Note note = new Note(MyKey.PRINT_TYPE.TEXT, strText);
            note.setBold(PrintUtil.BOLD_SETTING);
            note.setUnderline(PrintUtil.UNDERLINE_SETTING);

            List<Note> list = new ArrayList<>();
            list.add(note);
            NoteContainer noteContainer = new NoteContainer(-1L, "", list);
            TaskController.getTaskController().startPrintTask(noteContainer);
        } else {
            Toast.makeText(EditActivity.this, "打印内容不能为空！", Toast.LENGTH_SHORT).show();
        }
    }

    public String transformTime(String fileName) {
        Date createTime;
        try{
            createTime = df1.parse(fileName);
            return df2.format(createTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                if (existFileName != null)
                    delete(existFileName);
                setResultBeforeFinish();
                finish();
                break;

            case R.id.print:
                if (BluetoothUtil.getInstance().getConnectDevice() == null) {
                    if (bottomPopupWindow == null) {
                        bottomPopupWindow = new BottomPopupWindow(EditActivity.this);
                    }
                    bottomPopupWindow.showPopupWindow();
                } else {
                    print();
                }
                break;

            case R.id.save:
                save();
                String currentTime = df2.format(new Date());
                tv_createTime.setText(currentTime);
                break;

            case android.R.id.home:
                int rs = save();
                //0说明发生了变化
                if (rs == 0) {
                    setResultBeforeFinish();
                }
                finish();
                break;
        }
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onBackPressed() {
        int rs = save();
        //0说明发生了变化
        if (rs == 0) {
            setResultBeforeFinish();
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bottomPopupWindow != null) {
            bottomPopupWindow.unRegisterScanReceiver();
            Log.d("EditActivity", "onStop: 已经成功注销监听器");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setResultBeforeFinish () {
        Intent returnData = new Intent();
        returnData.putExtra(MainActivity.RETURN_IF_UPDATE,1 );
        setResult(RESULT_OK, returnData);
    }

    //监听打印结果
    private OnConnectListener onPrintListener = new OnConnectListener() {
        @Override
        public void returnResult(int taskCode) {
            if (taskCode == MyKey.RESULT.COMMON_SUCCESS) {
                Toast.makeText(EditActivity.this, "打印成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditActivity.this, "打印过程出现问题了", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
