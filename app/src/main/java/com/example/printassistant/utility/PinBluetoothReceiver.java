package com.example.printassistant.utility;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PinBluetoothReceiver extends BroadcastReceiver {

    public String pin = "0000";  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000

    private String TAG = PinBluetoothReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action-->>" + action);

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (action != null) {
            switch (action) {
                //请求配对
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    try {
                        //确认配对
                        Method setPairingConfirmation = device.getClass().getDeclaredMethod("setPairingConfirmation", boolean.class);
                        setPairingConfirmation.invoke(device,true);
                        Log.d(TAG, "onReceive: isOrderedBroadcast:"+isOrderedBroadcast()+"isInitialStickyBroadcast:"+isInitialStickyBroadcast());


                        //调用setPin方法进行配对
                        Method setPinMethod = device.getClass().getDeclaredMethod("setPin",new Class[]{byte[].class});
                        setPinMethod.invoke(device, new Object[]{pin.getBytes()});

                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    break;

                //绑定状态改变
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_NONE:
                            Log.d(TAG, "onReceive: 取消配对");
                            //注销广播接收器
                            context.unregisterReceiver(DeviceAdapter.pinBluetoothReceiver);
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            Log.d(TAG, "onReceive: 配对中");
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            Log.d(TAG, "onReceive: 配对成功");
                            //注销广播接收器
                            context.unregisterReceiver(DeviceAdapter.pinBluetoothReceiver);
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
