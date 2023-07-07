package com.dshaik3.asu.mobileoffloading;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dshaik3.asu.mobileoffloading.master.Master;
import com.dshaik3.asu.mobileoffloading.slave.Slave;

import java.security.Permissions;


public class MainActivity extends AppCompatActivity {
    public Permissions permissions;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button BMaster = findViewById(R.id.button7);
        Button BSlave = findViewById(R.id.button8);

          BMaster.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(MainActivity.this, Slave.class);
                  startActivity(intent);
              }
          });

          BSlave.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(MainActivity.this, Master.class);
                  startActivity(intent);
              }
          });

    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
               Log.i("permissions",result.toString());
            });

    @Override
    protected void onStart() {
        super.onStart();
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]
                        {
                                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH_CONNECT
                        },2);
    }
}
