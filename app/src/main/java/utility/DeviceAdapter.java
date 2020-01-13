package utility;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.printassistant.R;

import java.util.List;

import entity.Device_BT;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private Context mContext;

    private List<Device_BT> mDeviceList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView imageView;
        TextView deviceName;
        TextView deviceAddress;

        public ViewHolder(View view){
            super(view);
            cardView = (CardView)view;
            deviceName = view.findViewById(R.id.device_name);
            deviceAddress = view.findViewById(R.id.device_address);
        }
    }

    public DeviceAdapter(List<Device_BT> deviceList){
        mDeviceList = deviceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
         if (mContext == null) {
             mContext = viewGroup.getContext();
         }
         View view = LayoutInflater.from(mContext).inflate(R.layout.device_list_item,viewGroup,false);
         final ViewHolder holder = new ViewHolder(view);
         final int position = i;
         holder.cardView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 IntentFilter intentFilter = new IntentFilter();
                 intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
                 intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                 PinBluetoothReceiver.pinBluetoothReceiver = new PinBluetoothReceiver();
                 v.getContext().registerReceiver(PinBluetoothReceiver.pinBluetoothReceiver, intentFilter);
                 //配对蓝牙
                 BluetoothConnector.pinBD(mDeviceList.get(position).getDevice());
             }
         });
         return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device_BT device_bt = mDeviceList.get(position);
        holder.deviceName.setText(device_bt.getDeviceName());
        holder.deviceAddress.setText(device_bt.getDeviceAddress());
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }
}
