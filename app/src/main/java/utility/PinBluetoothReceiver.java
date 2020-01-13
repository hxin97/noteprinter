package utility;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PinBluetoothReceiver extends BroadcastReceiver {
    private String pin = "0000";  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000
    private String TAG = PinBluetoothReceiver.class.getName();

    public  static PinBluetoothReceiver pinBluetoothReceiver;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action-->>" + action);

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        switch (action) {
            //请求配对
            case BluetoothDevice.ACTION_PAIRING_REQUEST:
                try {
                    Method setPairingConfirmation = device.getClass().getDeclaredMethod("setPairingConfirmation", boolean.class);
                    setPairingConfirmation.invoke(device,true);
                    Log.d(TAG, "onReceive: isOrderedBroadcast:"+isOrderedBroadcast()+"isInitialStickyBroadcast:"+isInitialStickyBroadcast());
                    context.unregisterReceiver(pinBluetoothReceiver);

                    Method removeBondMethod = device.getClass().getDeclaredMethod("setPin",new Class[]{byte[].class});
                    removeBondMethod.invoke(device, new Object[]{pin.getBytes()});

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
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG, "onReceive: 配对中");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d(TAG, "onReceive: 配对成功");
                        break;
                }
                break;

            default:
                break;
        }
    }
}
