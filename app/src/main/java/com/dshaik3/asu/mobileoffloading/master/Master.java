package com.dshaik3.asu.mobileoffloading.master;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dshaik3.asu.mobileoffloading.Connected;
import com.dshaik3.asu.mobileoffloading.Device;
import com.dshaik3.asu.mobileoffloading.DeviceHolder;
import com.dshaik3.asu.mobileoffloading.MyAdapter;
import com.dshaik3.asu.mobileoffloading.R;

import java.util.ArrayList;
import java.util.List;



public class Master extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private ListView list;
    private List<Device> mDevices= new ArrayList<>();
    private MyAdapter mAdapter;

    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_activity);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Button discover = (Button) findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);
        this.list = (ListView)findViewById(R.id.list);
        // Create the empty view
        TextView emptyView = findViewById(R.id.emptyElement);
        // Set the empty view
        this.list.setEmptyView(emptyView);
        this.mAdapter = new MyAdapter(Master.this, R.layout.list_item, mDevices);
        this.list.setAdapter(mAdapter);
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForDevices();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void scanForDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            if(mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.startDiscovery();
        }
    }
    @SuppressLint("MissingPermission")
    public void Connect(Device device)
    {
        // Establish Bluetooth connection with the slave device
        Intent intent = new Intent(Master.this, Connected.class);
        DeviceHolder.setDevice(device);
        startActivity(intent);
    }



    private final BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver() {
        @SuppressLint({"MissingPermission", "NewApi"})
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                mDevices.clear();
                showLoading();
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                hideLoading();
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("uuid",intent.getExtras().toString());

                // Add the name and address to an array adapter to show in a ListView
                @SuppressLint("MissingPermission")
                Device mDevice = new Device(device.getName(),device.getAddress(),device);
                mDevices.add(mDevice);
                mAdapter.notifyDataSetChanged();
            }
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(mBroadCastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mAdapter.addDevices(mDevices);

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mBroadCastReceiver, filter);
    }
    private void showLoading() {
          progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        // Hide the loading indicator
        progressBar.setVisibility(View.GONE);
    }
}


