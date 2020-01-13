package utility;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BluetoothConnector {

    private static final String TAG = "BluetoothConnector";

    public static BluetoothAdapter mBluetoothAdapter;

    private BluetoothSocket socket;

    /**
     * 检查设备是否支持蓝牙
     * @return
     */
    public static boolean isSupportBluetooth(){
        return mBluetoothAdapter != null;
    }

    /**
     * 检查蓝牙是否打开
     * @return
     */
    public static boolean isBluetoothEnable(){
        return isSupportBluetooth() && mBluetoothAdapter.isEnabled();
    }

    /**
     * 异步打开蓝牙
     */
    public static boolean openBluetooth() {
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
    public static boolean scanBluetooth(Activity activity) {

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
    public static boolean cancelScanBluetooth(){
        if (isSupportBluetooth()){
            return mBluetoothAdapter.cancelDiscovery();
        }
        return true;
    }


    /**
     * 配对
     *
     */
    public static void pinBD(BluetoothDevice device){
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
    public static void cancelPinBD (BluetoothDevice device) {
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

}
