package com.example.printassistant;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.example.printassistant.entity.Device_BT;
import com.example.printassistant.task.TaskController;
import com.example.printassistant.utility.OnConnectListener;
import com.example.printassistant.utility.PinBluetoothReceiver;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private Context mContext;

    private List<Device_BT> mDeviceList;

    private OnConnectListener mConnectListener;

    public static PinBluetoothReceiver pinBluetoothReceiver;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView imageView;
        TextView deviceName;
        TextView deviceAddress;
        TextView connectState;

        private ViewHolder(View view){
            super(view);
            cardView = (CardView)view;
            deviceName = view.findViewById(R.id.device_name);
            deviceAddress = view.findViewById(R.id.device_address);
            connectState = view.findViewById(R.id.tv_connect_state);
        }
    }

    public DeviceAdapter(List<Device_BT> deviceList, OnConnectListener listenerInMain){
        mDeviceList = deviceList;
        mConnectListener = listenerInMain;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
         if (mContext == null) {
             mContext = viewGroup.getContext();
         }
         View view = LayoutInflater.from(mContext).inflate(R.layout.device_list_item,viewGroup,false);
         return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Device_BT device_bt = mDeviceList.get(position);
        if (device_bt.getConnectState()) {
            holder.connectState.setText("已连接");
        } else {
            holder.connectState.setText("");
        }
        holder.deviceName.setText(device_bt.getDeviceName());
        holder.deviceAddress.setText(device_bt.getDeviceAddress());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TaskController.getTaskController().startConnectTask(device_bt.getDevice(), mConnectListener);

                /**
                 * 匹配蓝牙(保留功能)
                 */
//                IntentFilter intentFilter = new IntentFilter();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
//                }
//                intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//                pinBluetoothReceiver = new PinBluetoothReceiver();
//                v.getContext().registerReceiver(pinBluetoothReceiver, intentFilter);
//                //配对蓝牙
//                BluetoothUtil.getInstance().pinBD(device_bt.getDevice());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

}