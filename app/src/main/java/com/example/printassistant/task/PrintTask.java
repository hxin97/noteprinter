package com.example.printassistant.task;

import android.bluetooth.BluetoothDevice;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.example.printassistant.data.HandlePrintData;
import com.example.printassistant.data.NoteContainer;
import com.example.printassistant.utility.BluetoothUtil;
import com.example.printassistant.utility.OnConnectListener;
import com.example.printassistant.utility.PrintUtil;

public class PrintTask {
    static PrintTask printTask;
    private BluetoothDevice connectDevice;
    private OnConnectListener listenerInMain;
    private OnConnectListener onPrintListener;
    public static int timeOutCounter;



    private static final String TAG = "PrintTask";

    public static PrintTask getPrintTask() {
        if(printTask == null) {
            printTask = new PrintTask();
        }
        return printTask;
    }

    public void sendTaskResult(Message msg) {
        if (this.listenerInMain != null) {
            this.listenerInMain.returnResult(msg.arg1);
        }
    }

    //中断打印
    public void interruptPrint() {}
    //执行打印
    public void print(NoteContainer data) {
        //this.onPrintListener = onPrintListener;
        if (data != null && data.getNoteList() != null && data.getNoteList().size() > 0) {
            timeOutCounter = 600;
            //BluetoothUtil.getInstance().setBluetoothListener(onBluetoothListener);
            this.connectDevice = BluetoothUtil.getInstance().getConnectDevice();
            if (connectDevice == null) {

            } else {
//                String deviceName = connectDevice.getName();
//                if (!TextUtils.isEmpty(deviceName) && deviceName.startsWith("Cubinote-")) {
//                    HandlePrintData.getInstance().handle(data.getNoteList());
//                } else{}
                PrintUtil.printIt(data.getNoteList());

            }
        }
    }

    //终止打印
    public void stopPrint() {
        Log.d(TAG, "stopPrint: 打印任务关闭");

    }

}
