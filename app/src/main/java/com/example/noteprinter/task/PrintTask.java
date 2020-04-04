package com.example.noteprinter.task;

import android.bluetooth.BluetoothDevice;
import android.os.Message;
import android.util.Log;

import com.example.noteprinter.data.NoteContainer;
import com.example.noteprinter.utility.BluetoothUtil;
import com.example.noteprinter.utility.OnBluetoothListener;
import com.example.noteprinter.utility.OnConnectListener;
import com.example.noteprinter.utility.PrintUtil;

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
        if (data != null && data.getNoteList() != null && data.getNoteList().size() > 0) {
            timeOutCounter = 600;
            //BluetoothUtil.getInstance().setBluetoothListener(onBluetoothListener);
            this.connectDevice = BluetoothUtil.getInstance().getConnectDevice();
            if (connectDevice == null) {

            } else {
                PrintUtil.printIt(data.getNoteList());

            }
        }
    }

    //终止打印
    public void stopPrint() {
        Log.d(TAG, "stopPrint: 打印任务关闭");

    }

}
