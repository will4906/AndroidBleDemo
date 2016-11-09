package com.example.will.blue.Ble;

/**
 * Created by will on 2016/10/21.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.example.will.blue.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 这个类需要几个函数
 * 1.判断设备是否支持蓝牙
 * 2.初始化
 * 3.打开蓝牙
 * 4.关闭蓝牙
 * 5.扫描设备（包括多种方式）
 * 6.连接设备
 * 7.读取数据
 * 8.得到数据通知
 */
public class BleControl {
    /*以下代码运用了设计模式中的单例模式，懒汉方式，静态内部类实现，既保证了线程安全又保证了资源不被损耗*/
    private static class BleControlHolder {
        private static final BleControl INSTANCE = new BleControl();
    }
    //构造方法私有，不被外界调用
    private BleControl (){}

    public static final BleControl getInstance() {
        return BleControlHolder.INSTANCE;
    }

    private static final String TAG = "BleControl";

    /**
     * activity或service的实例，只要有改变也得跟着改变
     */
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    private long scanPeriod = 10000;
    private boolean mScanning = false;
    private ArrayList<BleDeviceInfo> bleDeviceInfoArr = new ArrayList<>();
    private Handler mHandler = new Handler();

    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice connectedDevice = null;
    private int mConnectionState = STATE_DISCONNECTED;
    private UUID[] mCharacteristicUuidArr;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTING = 3;

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public BluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    /**
     * 初始化蓝牙，返回蓝牙是否初始化成功
     * @return
     */
    public boolean initBleControl(Context mContext){
        this.setContext(mContext);
        boolean flag = true;
        if (isBLESupported()) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        } else {
            flag = false;
        }
        return flag;
    }
    /**
     * 判断设备是否支持ble功能
     * @return
     */
    public boolean isBLESupported() {
        boolean flag = true;
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            flag = false;
        }
        return flag;
    }

    /**
     * 判断安卓设备蓝牙是否已经打开
     * @return 返回蓝牙是否已经开启
     */
    private boolean isBLEEnabled() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }


    /**
     * 开启蓝牙
     * @param bShowDialog true表示需要提示用户，并经过用户同意，false表示无需经过用户直接开启
     */
    public void enableBle(boolean bShowDialog) {
        if (isBLESupported()){
            if (bShowDialog){
                //这种是需要弹出对话框让用户选择是否打开的
                final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mContext.startActivity(enableIntent);
            }else{
                //这种是不经过用户，直接打开
                mBluetoothAdapter.enable();
            }
        }else {
            Log.v(TAG,"ble is not support");
        }
    }

    /**
     * 关闭蓝牙
     */
    public boolean disableBle(){
        boolean flag = false;
        if (isBLEEnabled()){
            flag = mBluetoothAdapter.disable();
        }else {
            flag = true;
        }
        return flag;
    }

    /**
     * 扫描ble设备
     * @param scanPeriod 扫描时长
     */
    public void scanLeDevice(long scanPeriod) {
        // Stops scanning after a pre-defined scan period.
        if (isBLEEnabled()){
            bleDeviceInfoArr.clear();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            broadcastUpdate(BleBroadcastAction.ACTION_DEVICE_DISCOVERING);
        }
    }

    /**
     * 扫描ble设备
     */
    public void scanLeDevice() {
        // Stops scanning after a pre-defined scan period.
        if (isBLEEnabled()){
            bleDeviceInfoArr.clear();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bleDeviceInfoArr.clear();
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            broadcastUpdate(BleBroadcastAction.ACTION_DEVICE_DISCOVERING);
        }
    }

    /**
     * 根据指定uuid和扫描周期来扫描设备
     * @param serviceUuids
     * @param scanPeriod
     */
    public void scanLeDevice(UUID[] serviceUuids, long scanPeriod){
        // Stops scanning after a pre-defined scan period.
        if (isBLEEnabled()){
            bleDeviceInfoArr.clear();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothAdapter.startLeScan(serviceUuids, mLeScanCallback);
            broadcastUpdate(BleBroadcastAction.ACTION_DEVICE_DISCOVERING);
        }
    }

    /**
     * 根据指定uuid扫描设备
     * @param serviceUuids
     */
    public void scanLeDevice(UUID[] serviceUuids){
        // Stops scanning after a pre-defined scan period.
        if (isBLEEnabled()){
            bleDeviceInfoArr.clear();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothAdapter.startLeScan(serviceUuids, mLeScanCallback);
            broadcastUpdate(BleBroadcastAction.ACTION_DEVICE_DISCOVERING);
        }
    }

    public void stopScanDevice(){
        if (isBLEEnabled()){
            if (isDiscovering()){
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    public boolean isDiscovering(){
        return mScanning;
    }

    /**
     * 蓝牙扫描回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    BleDeviceInfo bleDeviceInfo = new BleDeviceInfo(device,rssi,scanRecord);
                    //在这里获取uuid
                    bleDeviceInfoArr.add(bleDeviceInfo);
                }
            };

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address, UUID[] mCharacteristicUuidArr) {
        this.mCharacteristicUuidArr = mCharacteristicUuidArr;
        if (isDiscovering()){
            stopScanDevice();
        }
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        //以下代码为谷歌代码基础上略作修改，后觉得没必要，但以防万一，暂时保留
        // Previously connected device.  Try to reconnect.
//        if (connectedDevice != null && address.equals(connectedDevice.getAddress())
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                mConnectionState = STATE_CONNECTING;
//                broadcastUpdate(BleBroadcastAction.ACTION_GATT_CONNECTING);
//                return true;
//            } else {
//                return false;
//            }
//        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        broadcastUpdate(BleBroadcastAction.ACTION_GATT_CONNECTING);
        Log.d(TAG, "Trying to create a new connection.");
        connectedDevice = device;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public boolean connect(final String address, UUID mCharacteristicUuidArr) {
        this.mCharacteristicUuidArr = new UUID[]{mCharacteristicUuidArr};
        if (isDiscovering()){
            stopScanDevice();
        }
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        //以下代码为谷歌代码基础上略作修改，后觉得没必要，但以防万一，暂时保留
        // Previously connected device.  Try to reconnect.
        if (mConnectionState == STATE_CONNECTED && connectedDevice != null && address.equals(connectedDevice.getAddress())
                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                mConnectionState = STATE_CONNECTING;
//                broadcastUpdate(BleBroadcastAction.ACTION_GATT_CONNECTING);
//                return true;
//            } else {
//                return false;
//            }
            close();
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        broadcastUpdate(BleBroadcastAction.ACTION_GATT_CONNECTING);
        Log.d(TAG, "Trying to create a new connection.");
        connectedDevice = device;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = BleBroadcastAction.ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = BleBroadcastAction.ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(BleBroadcastAction.ACTION_GATT_SERVICES_DISCOVERED);
                connectCharacteristic(mCharacteristicUuidArr);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic.getUuid().toString(), characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(characteristic.getUuid().toString(), characteristic);
        }
    };

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mConnectionState = STATE_DISCONNECTING;
        broadcastUpdate(BleBroadcastAction.ACTION_GATT_DISCONNECTING);
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        //以下代码为google源代码，网上解说为防止通知失败，但暂未发现这种情况，而且好像没有影响，所以暂时保留
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                UUID.fromString(characteristic.getUuid().toString()));
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        mBluetoothGatt.writeDescriptor(descriptor);

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // For all profiles
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(BleBroadcastAction.EXTRA_DATA,data);
        }

        mContext.sendBroadcast(intent);
    }

    private void connectCharacteristic(UUID[] characteristicUuidArr){
        for (BluetoothGattService bluetoothGattService : getSupportedGattServices()){
            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()){
                for (UUID uuid : characteristicUuidArr){
                    if (bluetoothGattCharacteristic.getUuid().toString().equals(uuid.toString())){
                        readCharacteristic(bluetoothGattCharacteristic);
                        setCharacteristicNotification(bluetoothGattCharacteristic,true);
                    }
                }
            }
        }
    }

    /**
     * Writes the characteristic value to the given characteristic.
     *
     * @param characteristic the characteristic to write to
     * @return true if request has been sent
     */
    private boolean writeCharacteristic(final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0)
            return false;

        return gatt.writeCharacteristic(characteristic);
    }

    public boolean writeBle(byte[] data, UUID uuid) {
        BluetoothGattCharacteristic targetCharacteristic = null;
        for (BluetoothGattService bluetoothGattService : getSupportedGattServices()) {
            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                if (bluetoothGattCharacteristic.getUuid().toString().equals(uuid.toString())) {
                    targetCharacteristic = bluetoothGattCharacteristic;
                }
            }
        }
        if (targetCharacteristic == null){
            return false;
        }else {
            targetCharacteristic.setValue(data);
            return writeCharacteristic(targetCharacteristic);
        }
//        return writeCharacteristic()
    }
}
