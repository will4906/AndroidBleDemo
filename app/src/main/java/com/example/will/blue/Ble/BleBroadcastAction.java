package com.example.will.blue.Ble;

/**
 * Created by will on 2016/10/21.
 */
public class BleBroadcastAction {
    /**
     * 已连接设备
     */
    public final static String ACTION_GATT_CONNECTED =
            "com.example.will.blue.ACTION_GATT_CONNECTED";

    /**
     * 设备连接中
     */
    public final static String ACTION_GATT_CONNECTING =
            "com.example.will.blue.ACTION_GATT_CONNECTING";

    /**
     * 设备已断开
     */
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.will.blue.ACTION_GATT_DISCONNECTED";

    /**
     * 设备断开中
     */
    public final static String ACTION_GATT_DISCONNECTING =
            "com.example.will.blue.ACTION_GATT_DISCONNECTING";

    /**
     * 发现设备中
     */
    public static final String ACTION_DEVICE_DISCOVERING =
            "com.example.will.blue.ACTION_DEVICE_DISCOVERING";

    /**
     * 服务已发现
     */
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.will.blue.ACTION_GATT_SERVICES_DISCOVERED";

    /**
     * 手机接收到数据(状态)全部改用各自的uuid作为广播标示
     */
//    public final static String ACTION_DATA_AVAILABLE =
//            "com.example.will.blue.ACTION_DATA_AVAILABLE";

    /**
     * 手机接收到的数据
     */
    public final static String EXTRA_DATA =
            "com.example.will.blue.EXTRA_DATA";

}
