package com.dshaik3.asu.mobileoffloading;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.dshaik3.asu.mobileoffloading.master.Master;
import com.dshaik3.asu.mobileoffloading.slave.Slave;

import java.security.Permissions;


public class MainActivity extends AppCompatActivity {
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button BMaster = findViewById(R.id.button7);
        Button BSlave = findViewById(R.id.button8);
        //
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        // Set the title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView titleText = toolbar.findViewById(R.id.titleText);
        titleText.setText("Mobile Offloading");

        // Handle back button click
        ImageView backButton = toolbar.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //

          BMaster.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(MainActivity.this, Master.class);
                  startActivity(intent);
              }
          });

          BSlave.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(MainActivity.this, Slave.class);
                  startActivity(intent);
              }
          });
        requestPermissions();
    }


   private ActivityResultLauncher requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
        if (permissions.get(Manifest.permission.BLUETOOTH) && permissions.get(Manifest.permission.ACCESS_FINE_LOCATION) && permissions.get(Manifest.permission.BLUETOOTH_CONNECT)
        && permissions.get(Manifest.permission.BLUETOOTH_SCAN) && permissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) && permissions.get(Manifest.permission.READ_MEDIA_IMAGES)&&permissions.get(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Permissions granted
            enableLocationServices();
        } else {
            // Permissions denied
            // Handle the case where permissions are not granted by the user
            // display a message or disable certain functionality that requires the permissions
        }
    });
    private void requestPermissions() {
        String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED&&
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED&&
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED


        ) {
            enableLocationServices();
        } else {
            // Request permissions from the user
            requestPermissionLauncher.launch(permissions);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("MissingPermission")
    private void enableLocationServices() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}
