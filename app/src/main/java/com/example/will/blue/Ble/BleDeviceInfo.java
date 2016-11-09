package com.example.will.blue.Ble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by will on 2016/10/19.
 */
public class BleDeviceInfo {
    private static final int NO_RSSI = -1000;

    private BluetoothDevice device;
    private String strDeviceName;
    private String strDeviceAddress;
    private int rssi;
    private byte[] scanRecord;
    public boolean isBonded;

    public BleDeviceInfo(BluetoothDevice device){
        this.device = device;
        setNameAndAddress();
    }
    public BleDeviceInfo(){

    }
    public BleDeviceInfo(BluetoothDevice device, int rssi, byte[] scanRecord){
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
        setNameAndAddress();
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
        setNameAndAddress();
    }

    public String getStrDeviceName() {
        return strDeviceName;
    }

    public void setStrDeviceName(String strDeviceName) {
        this.strDeviceName = strDeviceName;
    }

    public String getStrDeviceAddress() {
        return strDeviceAddress;
    }

    public void setStrDeviceAddress(String strDeviceAddress) {
        this.strDeviceAddress = strDeviceAddress;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    public boolean isBonded() {
        return isBonded;
    }

    public void setBonded(boolean bonded) {
        isBonded = bonded;
    }

    private void setNameAndAddress(){
        this.strDeviceName = this.device.getName();
        this.strDeviceAddress = this.device.getAddress();
    }
}
