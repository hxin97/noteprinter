package com.example.noteprinter.utility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BluetoothUtil {

    public  BluetoothAdapter mBluetoothAdapter;

    private BluetoothDevice mConnectDevice;

    private BluetoothUtil.ConnectThread mConnectThread;

    private BluetoothUtil.SocketThread mSocketThread;

    private OnBluetoothListener bluetoothListener;

    private BluetoothSocket socket;

    private static BluetoothUtil bluetoothUtil;

    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private int mState;
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECT_SUCCEED = 3;
    public static final int STATE_CONNECT_FAILURE = 4;
    public static final int MESSAGE_DISCONNECTED = 5;
    public static final int STATE_START_CONNECT = 9;
    public static final int STATE_CHANGE = 6;
    public static final int MESSAGE_READ = 7;
    public static final int MESSAGE_WRITE = 8;

    private static final String TAG = "BluetoothUtil";

    public BluetoothUtil(OnBluetoothListener var1) {
        this.bluetoothListener = var1;
        this.mState = STATE_NONE;
    }
    //获取蓝牙工具类的实例
    public static synchronized BluetoothUtil getInstance(){
        if(bluetoothUtil == null) {
            Log.d(TAG, "getInstance: init BluetoothUtil");
            bluetoothUtil = new BluetoothUtil((OnBluetoothListener)null);
        }
        return bluetoothUtil;
    }
    //销毁，退出app前执行
    public synchronized void onDestroy() {
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mSocketThread != null) {
            this.mSocketThread.cancel();
            this.mSocketThread = null;
        }
        bluetoothUtil = null;
        this.setState(STATE_NONE);
    }


    public BluetoothAdapter getBluetoothAdapter() {
        if (this.mBluetoothAdapter == null) {
            this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return this.mBluetoothAdapter;
    }

    public void setBluetoothListener(OnBluetoothListener var1) {
        this.bluetoothListener = var1;
    }

    public int getState() {
        return this.mState;
    }

    public void setState(int var1) {
        this.mState = var1;
    }

    public BluetoothDevice getConnectDevice() {
        return this.mConnectDevice;
    }

     /**
     * 检查设备是否支持蓝牙
     * @return true,false
     */
    private boolean isSupportBluetooth(){
        return mBluetoothAdapter != null;
    }

    /**
     * 检查蓝牙是否打开
     * @return
     */
    private boolean isBluetoothEnable(){
        return isSupportBluetooth() && mBluetoothAdapter.isEnabled();
    }

    /**
     * 异步打开蓝牙
     */
    private boolean openBluetooth() {
        if (isSupportBluetooth()){
            mBluetoothAdapter.enable();
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return isBluetoothEnable();
        }
        else
            return false;
//        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        activity.startActivityForResult(enableBTIntent,1);
    }

    /**
     * 扫描蓝牙，通过接收广播获取扫描到的蓝牙
     */
    public boolean scanBluetooth(Context activity) {

        if (!isBluetoothEnable()) {
            if (openBluetooth()) return scanBluetooth(activity);

            else {
                Log.e(TAG, "scanBluetooth: Bluetooth not enable");
                Toast.makeText(activity,"开启蓝牙失败",Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        //检查当前是否在扫描，如果是就取消当前的扫描，重新扫描
        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }

        //此方法是一个异步操作，一般搜索12s
        return mBluetoothAdapter.startDiscovery();
    }

    /**
     * 取消扫描蓝牙
     */
    public void cancelScanBluetooth(){
        if (isSupportBluetooth()) mBluetoothAdapter.cancelDiscovery();
    }


    /**
     * 发送蓝牙配对请求
     *
     */
    public void pinBD(BluetoothDevice device){
        if (device == null) {
            Log.e(TAG, "pin: Bond device null" );
            return;
        }
        if (!isBluetoothEnable()){
            Log.e(TAG, "pin: Bluetooth not enable" );
            return;
        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        //检查设备是否已经配对过
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            Log.d(TAG, "pin: Attempt to bond:"+device.getName());
            try {
                Method createBondMethod = device.getClass().getMethod("createBond");
                createBondMethod.invoke(device);

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Log.e(TAG, "pin: Attempt to bond fail" );
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Log.e(TAG, "pin: Attempt to bond fail" );
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                Log.e(TAG, "pin: Attempt to bond fail" );
            }
        } else {
            Log.d(TAG, "pinBD: 已经配对成功");
        }
    }

    /**
     * 取消配对
     */
    public void cancelPinBD (BluetoothDevice device) {
        if (device == null) {
            Log.d(TAG, "cancelPinBD: cancel bond device null");
            return;
        }
        if (!isBluetoothEnable()){
            Log.e(TAG, "cancelPinBD: Bluetooth not enable" );
            return;
        }
        //检查设备是否已经配对过
        if (device.getBondState() != BluetoothDevice.BOND_NONE) {
            Log.d(TAG, "cancelPinBD: Attempt to cancel bond:" + device.getName());
            try {
                Method removeBondMethod = device.getClass().getMethod("removeBond");
                removeBondMethod.invoke(device);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Log.e(TAG, "pin: Attempt to cancel bond fail" );
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Log.e(TAG, "pin: Attempt to cancel bond fail" );
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                Log.e(TAG, "pin: Attempt to cancel bond fail" );
            }
        }
    }

    //var1表示执行连接操作的结果
    private void sendMessage(int var1, byte[] var2) {
        if (this.bluetoothListener != null) {
            this.bluetoothListener.returnResult(var1, var2);
        } else {
            Log.e(TAG, "sendMessage: Bluetooth listener is null");
        }
    }

    public void write(byte[] bytes) {
        BluetoothUtil.SocketThread socketThread;
        synchronized (this) {
            if (this.mState != 3) {
                return;
            }
            socketThread = this.mSocketThread;
        }
        socketThread.write(bytes);
    }


    //连接设备,可以连接多个蓝牙设备(mConnectThread没有清除)
    public synchronized void connect (BluetoothDevice device) {
        if (this.mState == STATE_CONNECTING && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mSocketThread != null) {
            this.mSocketThread.cancel();
            this.mSocketThread = null;
        }
        this.mConnectThread = new BluetoothUtil.ConnectThread(device);
        this.mConnectThread.start();
        this.setState(STATE_CONNECTING);
    }

    public void disConnect () {
        this.connectionLost();
    }

    private void connectionSucceed (BluetoothSocket socket, BluetoothDevice device) {
        if(this.mSocketThread != null) {
            this.mSocketThread.cancel();
            this.mSocketThread = null;
        }
        this.mSocketThread = new BluetoothUtil.SocketThread(socket);
        this.mSocketThread.start();
        this.mConnectDevice = device;
        this.setState(STATE_CONNECT_SUCCEED);
        this.sendMessage(STATE_CONNECT_SUCCEED, (byte[])null); //3
    }

    private void connectionFailed () {
        this.sendMessage(STATE_CONNECT_FAILURE,(byte[])null);  //4
        this.mConnectDevice = null;
        this.setState(STATE_NONE);
    }

    private void connectionLost() {
        this.sendMessage(MESSAGE_DISCONNECTED,(byte[])null);  //5
        this.mConnectDevice = null;
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        this.setState(STATE_NONE);
    }



    private class ConnectThread extends Thread {
        private final BluetoothDevice mDevice;
        private BluetoothSocket mSocket;

        public ConnectThread (BluetoothDevice device) {
            this.mDevice = device;

            try {
                this.mSocket = device.createInsecureRfcommSocketToServiceRecord(BluetoothUtil.SERVICE_UUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: create() failed");
                e.printStackTrace();
            }
        }

        public void run() {
            Log.d(TAG, "run: begin mConnectThread");

            try {
                if (this.mSocket == null) {
                    BluetoothUtil.this.connectionFailed();
                    return;
                }

                this.mSocket.connect();

            } catch (IOException e) {
                BluetoothUtil.this.connectionFailed();
                Log.e(TAG, "run: connect failed");
                try {
                    this.mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }

            BluetoothUtil.this.connectionSucceed(this.mSocket, this.mDevice);
        }

        public void cancel() {
            try {
                this.mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of connect socket failed" + e);
            }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket serverSocket = null;
            try {
                serverSocket = BluetoothUtil.this.mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothChat", BluetoothUtil.SERVICE_UUID);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: listen() failed");
            }

            this.mServerSocket = serverSocket;
        }

        public void run() {
            this.setName("AcceptThread");
            BluetoothSocket socket = null;
            while(BluetoothUtil.this.mState != 3) {
                try {
                    socket = this.mServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "run: accept() failed");
                    break;
                }

                if(socket != null) {
                    BluetoothUtil util = BluetoothUtil.this;
                    synchronized (BluetoothUtil.this) {
                        switch (BluetoothUtil.this.mState) {
                            case 0:
                            case 3:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "run: could not close unwanted socket");
                                }
                                break;
                            case 1:
                            case 2:
                                BluetoothUtil.this.connectionSucceed(socket, socket.getRemoteDevice());
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                this.mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of server failed");
            }
        }
    }

    private class SocketThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public SocketThread(BluetoothSocket socket) {
            this.mSocket = socket;
            Log.d(TAG, "SocketThread: create ConnectThread " + this.mSocket);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "SocketThread: 没有创建临时sockets");
            }
            this.mInputStream = inputStream;
            this.mOutputStream = outputStream;
        }

        public void run() {
            while(true) {
                boolean changer = false;   //控制循环
                ArrayList inputContent = new ArrayList();
                inputContent.clear();

                try {
                    while (!changer) {
                        inputContent.add((byte) this.mInputStream.read());
                        if (inputContent.size() >= 4) {

                            if (BluetoothUtil.getInstance().getConnectDevice() == null){
                                BluetoothUtil.this.connectionLost();
                            } else {
                                String deviceName = BluetoothUtil.getInstance().getConnectDevice().getName();
                                int var;

                                // ****** 编辑模块待补充
                                if (!TextUtils.isEmpty(deviceName)) {
                                    if(deviceName.startsWith("Cubinote-")){
                                        var = IOUtil.byteToShort(new byte[]{(Byte)inputContent.get(2),(Byte)inputContent.get(3)}) + 4;
                                    } else {
                                        var = IOUtil.byteToShort(new byte[]{(Byte)inputContent.get(1),(Byte)inputContent.get(2)}) + 4;
                                    }
                                } else {
                                    var = IOUtil.byteToShort(new byte[]{(Byte)inputContent.get(2),(Byte)inputContent.get(3)}) + 4;
                                }

                                if (var != 0 && inputContent.size() >= var) {
                                    byte[] var2 = new byte[inputContent.size() + 2];

                                    for (int i = 0; i < inputContent.size(); ++i) {
                                        var2[i] = (Byte)inputContent.get(i);
                                    }

                                    if (BluetoothUtil.this.bluetoothListener != null) {
                                        changer = true;
                                        BluetoothUtil.this.sendMessage(7, var2);
                                    } else {
                                        changer = false;
                                    }
                                }
                            }
                        }
                    }
                } catch(IOException e){
                    e.printStackTrace();
                    Log.e(TAG, "run: disconnected" );
                    BluetoothUtil.this.connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] var1) {

            try {
                this.mOutputStream.flush();
                this.mOutputStream.write(var1);
                this.mOutputStream.flush();
                Log.d(TAG, "write: write data success");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "write: Exception during write");
            }
        }

        public void cancel() {
            try {
                this.mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of connect socket failed" + e );
            }
        }

    }

}
