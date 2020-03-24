package com.example.printassistant.entity;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class Device_BT implements Serializable {

    private String deviceName;
    private String deviceAddress;
    private BluetoothDevice device;
    private boolean connectState = false;

    public String getDeviceName(){
        return deviceName;
    }

    public void setDeviceName(String deviceName){
        this.deviceName = deviceName;
    }

    public String getDeviceAddress(){
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress){
        this.deviceAddress = deviceAddress;
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    public void setDevice(BluetoothDevice device){
        this.device = device;
    }

    public boolean getConnectState() {
        return connectState;
    }

    public void setConnectState(boolean connectState) {
        this.connectState = connectState;
    }
}
