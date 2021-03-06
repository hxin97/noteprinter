package com.example.noteprinter.task;

import android.bluetooth.BluetoothDevice;
import android.os.Message;

import com.example.noteprinter.utility.BluetoothUtil;
import com.example.noteprinter.utility.OnBluetoothListener;
import com.example.noteprinter.utility.OnConnectListener;

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

    //public void stopConnect() {}

}
