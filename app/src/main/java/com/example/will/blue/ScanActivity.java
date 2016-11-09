package com.example.will.blue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.will.blue.Ble.BleBroadcastAction;
import com.example.will.blue.Ble.BleControl;

import java.util.UUID;

public class ScanActivity extends AppCompatActivity {

    private BleControl bleControl = null;
    private Button connectButton,scanButton,disconnectButton;
    private final static String TAG = "ScanActivity";
    private String strUUid1 = "00002a00-0000-1000-8000-00805f9b34fb";
    private String strUUid2 = "00002a01-0000-1000-8000-00805f9b34fb";
    private UUID[] uuids = new UUID[]{UUID.fromString(strUUid1),UUID.fromString(strUUid2)};

    private final static int HIDE_MSB_8BITS_OUT_OF_32BITS = 0x00FFFFFF;
    private final static int HIDE_MSB_8BITS_OUT_OF_16BITS = 0x00FF;
    private final static int SHIFT_LEFT_8BITS = 8;
    private final static int SHIFT_LEFT_16BITS = 16;
    private final static int GET_BIT24 = 0x00400000;
    private final static int FIRST_BIT_MASK = 0x01;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        bleControl = BleControl.getInstance();
        bleControl.setContext(this);
        connectButton = (Button)findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleControl.connect("1E:04:15:83:00:92",uuids );
            }
        });
        scanButton = (Button)findViewById(R.id.fragment_scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleControl.scanLeDevice();
            }
        });
        disconnectButton = (Button)findViewById(R.id.fragment_disconnect_button);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleControl.disconnect();
            }
        });
    }

    @Override
    protected void onDestroy() {
        bleControl.stopScanDevice();
        bleControl.close();
        bleControl.disconnect();
        unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleBroadcastAction.ACTION_DEVICE_DISCOVERING.equals(action)){
                Log.v(TAG,"扫描中");
            } else if (BleBroadcastAction.ACTION_GATT_CONNECTING.equals(action)) {
                Log.v(TAG,"连接中");
            }else if (BleBroadcastAction.ACTION_GATT_CONNECTED.equals(action)) {
                Log.v(TAG,"已连接");
            } else if (BleBroadcastAction.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.v(TAG,"取消连接");
            } else if (BleBroadcastAction.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.v(TAG,"已经发现服务");
                bleControl.getSupportedGattServices();
            } else if (UUID.fromString(strUUid1).toString().equals(action)) {
                byte[] data = intent.getByteArrayExtra(BleBroadcastAction.EXTRA_DATA);
                String strData = "";
                for (byte b : data){
                    strData += String.valueOf(b);
                }
                Log.v(TAG,strData);
            } else if (UUID.fromString(strUUid2).toString().equals(action)){
                byte[] data = intent.getByteArrayExtra(BleBroadcastAction.EXTRA_DATA);
                try {
                    TextView textView = (TextView)findViewById(R.id.receive_text);
                    textView.setText(String.valueOf(decodeTemperature(data)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleBroadcastAction.ACTION_DEVICE_DISCOVERING);
        intentFilter.addAction(BleBroadcastAction.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BleBroadcastAction.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleBroadcastAction.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleBroadcastAction.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UUID.fromString(strUUid1).toString());
        intentFilter.addAction(UUID.fromString(strUUid2).toString());
        return intentFilter;
    }

    private double decodeTemperature(byte[] data) throws Exception {
        double temperatureValue;
        byte flag = data[0];
        byte exponential = data[4];
        short firstOctet = convertNegativeByteToPositiveShort(data[1]);
        short secondOctet = convertNegativeByteToPositiveShort(data[2]);
        short thirdOctet = convertNegativeByteToPositiveShort(data[3]);
        int mantissa = ((thirdOctet << SHIFT_LEFT_16BITS) | (secondOctet << SHIFT_LEFT_8BITS) | (firstOctet)) & HIDE_MSB_8BITS_OUT_OF_32BITS;
        mantissa = getTwosComplimentOfNegativeMantissa(mantissa);
        temperatureValue = (mantissa * Math.pow(10, exponential));

		/*
		 * Conversion of temperature unit from Fahrenheit to Celsius if unit is in Fahrenheit
		 * Celsius = (98.6*Fahrenheit -32) 5/9
		 */
        if ((flag & FIRST_BIT_MASK) != 0) {
            temperatureValue = (float) ((98.6 * temperatureValue - 32) * (5 / 9.0));
        }
        return temperatureValue;
    }

    private short convertNegativeByteToPositiveShort(byte octet) {
        if (octet < 0) {
            return (short) (octet & HIDE_MSB_8BITS_OUT_OF_16BITS);
        } else {
            return octet;
        }
    }

    private int getTwosComplimentOfNegativeMantissa(int mantissa) {
        if ((mantissa & GET_BIT24) != 0) {
            return ((((~mantissa) & HIDE_MSB_8BITS_OUT_OF_32BITS) + 1) * (-1));
        } else {
            return mantissa;
        }
    }
}
