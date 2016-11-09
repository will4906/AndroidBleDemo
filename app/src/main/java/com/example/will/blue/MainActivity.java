package com.example.will.blue;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.will.blue.Ble.BleControl;

public class MainActivity extends AppCompatActivity {

    private BleControl bleControl;
    private Button startButtonTrue,startButtonFalse,scanButton,closeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bleControl = BleControl.getInstance();
        bleControl.initBleControl(this);

        initStartButtonTrue();
        initStartButtonFalse();
        initScanButton();
        initCloseButton();
    }

    private void initStartButtonTrue(){
        startButtonTrue = (Button)findViewById(R.id.start_button_true);
        startButtonTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleControl.enableBle(true);
            }
        });
    }

    private void initStartButtonFalse(){
        startButtonFalse = (Button)findViewById(R.id.start_button_false);
        startButtonFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleControl.enableBle(false);
            }
        });
    }

    private void initScanButton(){
        scanButton = (Button)findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ScanActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initCloseButton(){
        closeButton = (Button)findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleControl.disableBle();
            }
        });
    }
}
