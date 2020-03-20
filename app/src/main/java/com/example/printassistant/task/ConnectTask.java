package com.example.printassistant.task;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.example.printassistant.utility.BluetoothUtil;
import com.example.printassistant.utility.OnBluetoothListener;
import com.example.printassistant.utility.OnConnectListener;

public class ConnectTask {
    private static ConnectTask connectTask;
    private OnConnectListener listenerInMain;

    public ConnectTask() {
    }

    public static ConnectTask getConnectTask() {
        if (connectTask == null) {
            connectTask = new ConnectTask();
        }
        return connectTask;
    }

    public void sendTaskResult(Message msg) {
        if (this.listenerInMain != null) {
            this.listenerInMain.returnResult(msg.arg1);
        }
    }

    public void connectDevice (BluetoothDevice device, OnConnectListener onConnectListener, OnBluetoothListener onBluetoothListener) {
        this.listenerInMain = onConnectListener;
        if (BluetoothUtil.getInstance().getState() == 3) {
            //当前有设备连接
        }

        BluetoothUtil.getInstance().setBluetoothListener(onBluetoothListener);
        BluetoothUtil.getInstance().connect(device);
    }

    public void stopConnect() {}

}
