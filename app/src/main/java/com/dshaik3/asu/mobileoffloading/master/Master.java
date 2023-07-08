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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dshaik3.asu.mobileoffloading.Connected;
import com.dshaik3.asu.mobileoffloading.ConnectedDeviceAdapter;
import com.dshaik3.asu.mobileoffloading.Device;
import com.dshaik3.asu.mobileoffloading.DeviceHolder;
import com.dshaik3.asu.mobileoffloading.MyAdapter;
import com.dshaik3.asu.mobileoffloading.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class Master extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private ListView list;
    private ListView mConnectedList;
    private List<Device> mDevices= new ArrayList<>();

    private List<Device> mBonded = new ArrayList<>();
    private MyAdapter mAdapter;

    private ProgressBar progressBar;

    private ConnectedDeviceAdapter mDeviceAdapter;


    private Button ComputeMatrix;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_activity);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Button discover = (Button) findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);
        this.list = (ListView)findViewById(R.id.list);
        mConnectedList = (ListView)findViewById(R.id.ConnectList);
        ComputeMatrix = findViewById(R.id.button9);


        RelativeLayout emptyView = findViewById(R.id.emptyElement);
        this.list.setEmptyView(emptyView);

        RelativeLayout emptyConnect = findViewById(R.id.emptyConnected);
        mConnectedList.setEmptyView(emptyConnect);
        //
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        // Set the title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView titleText = toolbar.findViewById(R.id.titleText);
        titleText.setText("Master");

        // Handle back button click
        ImageView backButton = toolbar.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //

        this.mAdapter = new MyAdapter(Master.this, R.layout.list_item, mDevices);
        this.list.setAdapter(mAdapter);

        mDeviceAdapter = new ConnectedDeviceAdapter(Master.this,R.layout.list_item,mBonded);
        mConnectedList.setAdapter(mDeviceAdapter);

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForDevices();
            }
        });
        IntentFilter bondStateFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondStateReceiver, bondStateFilter);

        ComputeMatrix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                mAdapter.clear();
                mBonded.clear();
                mDevices.clear();
                showLoading();
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                for(Device device :mDevices) {

                    if (device.getDevice().getBondState()!=BluetoothDevice.BOND_BONDED) {
                        device.getDevice().createBond();
                    }
                    if(mDevices.indexOf(device) == mDevices.size()-1)
                        hideLoading();
                }

            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);


                // Add the name and address to an array adapter to show in a ListView
                @SuppressLint("MissingPermission")
                Device mDevice = new Device(device.getName(),device.getAddress(),device);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    if(!mBonded.contains(device))
                    {
                        mBonded.add(mDevice);
                        mDeviceAdapter.notifyDataSetChanged();}
                    }

                if(rssi>=-70 && rssi<=-50 && device.getName()!=null) {
                    mDevices.add(mDevice);
                    mAdapter.notifyDataSetChanged();
                }

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

    // MainActivity.java

    private BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                @SuppressLint("MissingPermission") Device device1 = new Device(device.getName(),device.getAddress(),device);
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    // Device is successfully bonded, add it to the accepted devices list
                    if(!mBonded.contains(device)) {
                        mBonded.add(device1);
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };
}


