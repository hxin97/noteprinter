package com.example.printassistant.task;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.printassistant.common.MyKey;
import com.example.printassistant.data.NoteContainer;
import com.example.printassistant.utility.BluetoothUtil;
import com.example.printassistant.utility.OnBluetoothListener;
import com.example.printassistant.utility.OnConnectListener;

public class TaskController {
    private static TaskController taskController;
    private OnBluetoothListener onBluetoothListener;
    private static final String TAG = "TaskController";


    public TaskController() {}

    public static TaskController getTaskController() {
        if (taskController == null) {
            taskController = new TaskController();
        }
        return taskController;
    }
    //打开蓝牙后调用，设置蓝牙功能相关监听器
    public void init() {
        this.setListener();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    //
                    break;
                case 1:
                    //发送连接结果
                    ConnectTask.getConnectTask().sendTaskResult(msg);
                    break;
                case 2:
                    //
                    break;
            }
        }
    };

    private void setListener () {
        this.onBluetoothListener = new OnBluetoothListener() {
            @Override
            public void returnResult(int var1, byte[] var2) {
                Message msg;

                //判断连接状态
                switch (var1) {
                    case 3:
                        //连接蓝牙设备成功
                        msg = TaskController.this.handler.obtainMessage(1);
                        msg.arg1 = MyKey.RESULT.COMMON_SUCCESS;
                        TaskController.this.handler.sendMessage(msg);
                        break;
                    case 4:
                        //连接蓝牙设备失败
                        msg = TaskController.this.handler.obtainMessage(1);
                        msg.arg1 = MyKey.RESULT.COMMON_FAIL;
                        TaskController.this.handler.sendMessage(msg);
                        break;
                    case 5:
                        //连接丢失
                        msg = TaskController.this.handler.obtainMessage(1);
                        msg.arg1 = MyKey.RESULT.COMMON_LOST;
                        TaskController.this.handler.sendMessage(msg);
                        break;
                    default:
                }
            }
        };
    }

    public void onDestroy () {
        BluetoothUtil.getInstance().setBluetoothListener((OnBluetoothListener) null);
    }

    public void startConnectTask(BluetoothDevice device, OnConnectListener onConnectListener) {
        Log.d(TAG, "startConnectTask: 启动连接任务");
        ConnectTask.getConnectTask().connectDevice(device, onConnectListener, onBluetoothListener);
    }

    public void startPrintTask(NoteContainer content) {
        PrintTask.getPrintTask().print(content);
    }
}
