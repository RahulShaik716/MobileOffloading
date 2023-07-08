package com.dshaik3.asu.mobileoffloading.slave;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.dshaik3.asu.mobileoffloading.DeviceHolder;
import com.dshaik3.asu.mobileoffloading.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

public class Slave extends AppCompatActivity {
    private static final String TAG = "SlaveActivity";
    private static final String APP_NAME = "SlaveApp";
    private static final java.util.UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private String receivedData;

    private LocationManager locationManager;
    private BroadcastReceiver batteryReceiver;
    private String currentLocation;
    private int currentBatteryPercentage;
    private static final int DISCOVERABLE_DURATION = 300; // 5 minutes in seconds
    private TextView textView ;
    private TextView battery;
    private TextView Name;
    private TextView address;
    private TextView location;
    private Button Monitor;
    private int[] result = new int[2];
    private ObjectInputStream objectInputStream;
    private long execution_time;
    private ObjectOutputStream objectOutputStream;

    boolean master_request = false;

    private boolean isRunning = true;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slave_activity);


        Name = findViewById(R.id.textView);
        address = findViewById(R.id.textView7);
        location = findViewById(R.id.textView8);
        battery = findViewById(R.id.textView9);
        Monitor = findViewById(R.id.button5);

        //

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        // Set the title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView titleText = toolbar.findViewById(R.id.titleText);
        titleText.setText("Slave");

        // Handle back button click
        ImageView backButton = toolbar.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //
        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth");
            return;
        }
        // Enable Bluetooth if not already enabled
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        Name.setText(bluetoothAdapter.getName());
        address.setText(bluetoothAdapter.getAddress());
        makeDiscoverable();

        // Start listening for incoming Bluetooth connections
        new Thread(new OpenSocket()).start();



        Monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdates();
                registerBatteryReceiver();
            }
        });

    }
    @SuppressLint("MissingPermission")
    private final class OpenSocket extends Thread{
        public void run() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, UUID);
                new Thread(new AcceptThread()).start();
            } catch (IOException e) {
                Log.e(TAG, "Error creating server socket", e);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivity(discoverableIntent);
    }

    private class AcceptThread implements Runnable {
        @Override
        public void run() {
            try {
                socket = serverSocket.accept();
                if(socket!=null) {
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                    objectOutputStream = new ObjectOutputStream(outputStream);
                    objectInputStream = new ObjectInputStream(inputStream);
                    while(isRunning && socket!=null) {
                        if (objectInputStream.available() != -1) {

                            HashMap<String, Object> receivedKeyValuePairs = (HashMap<String, Object>) objectInputStream.readObject();
                            if (receivedKeyValuePairs.containsKey("request")) {
                                master_request = true;
                                send_monitor();
                            } else {
                                int[] row2 = (int[]) receivedKeyValuePairs.get("row2");
                                int[][] matrix2 = (int[][]) receivedKeyValuePairs.get("matrix2");
                                Log.i("row2", row2.toString());
                                Log.i("matrix2", DeviceHolder.ArrayToString(matrix2));
                                //compute row2 in slave ..
                                long start_time = System.nanoTime();
                                for (int i = 0; i < 2; i++) {
                                    int sum = 0;
                                    for (int j = 0; j < 2; j++) {
                                        sum += row2[j] * matrix2[j][i];
                                    }
                                    result[i] = sum;
                                }
                                long end_time = System.nanoTime();
                                execution_time = end_time - start_time;
                                //send it back to master..
                                sendToMaster();
                            }
                        }
                    }
                }
            } catch (IOException e){
                    Log.e(TAG, "Error accepting connection", e);
                } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendToMaster()
    {
        Thread send = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    HashMap data = new HashMap<>();
                    data.put("row2",result);
                    data.put("slave_exe",execution_time);
                    Log.i("slave_time", String.valueOf(execution_time));
                    objectOutputStream.writeObject(data);
                    objectOutputStream.flush();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
       send.start();
        try {
            send.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Log.i("sent","success");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            try {
                BluetoothDevice bondedDevice = socket.getRemoteDevice();
                Method removeBondMethod = bondedDevice.getClass().getMethod("removeBond");
                removeBondMethod.invoke(bondedDevice);
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Bluetooth socket", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            socket = null;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing output stream", e);
            }
            inputStream = null;
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing output stream", e);
            }
            outputStream = null;
        }
       unregisterBatteryReceiver();
    }

    // Code for the slave activity


    // Method to start monitoring location updates
    private void startLocationUpdates() {
        // Initialize LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, locationListener);
        } else {
            // Location permission not granted, handle accordingly
        }
    }

    // LocationListener to handle location updates
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Update current location
            currentLocation = location.getLatitude() + ", " + location.getLongitude();
            // Send the updated location to the requester if needed
            sendLocationUpdate();

        }
    };

    private void registerBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                // Calculate the battery percentage
                currentBatteryPercentage = (int) ((level / (float) scale) * 100);

                // Send the updated battery percentage to the requester if needed
                sendBatteryPercentageUpdate();

            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    // Method to unregister the battery receiver
    private void unregisterBatteryReceiver() {
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
    }

    // Method to send location update to the requester
    private void sendLocationUpdate() {
        // Create a message with the updated location
        String message = "Location Update: " + currentLocation;
        location.setText(message);
        // Send the message to the requester using Bluetooth communication
        if(master_request)
        send_monitor();
    }

    // Method to send battery percentage update to the requester
    private void sendBatteryPercentageUpdate() {
        // Create a message with the updated battery percentage
        String message = "Battery Percentage Update: " + currentBatteryPercentage + "%";
        battery.setText(message);
        // Send the message to the requester using Bluetooth communication
        // ...
        if(master_request)
        send_monitor();
    }


    private void send_monitor()
    {
        Thread send = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(socket!=null) {
                        HashMap data = new HashMap<>();
                        data.put("location", currentLocation);
                        data.put("battery", currentBatteryPercentage);
                        objectOutputStream.writeObject(data);
                        objectOutputStream.flush();
                        Log.i("slave", "sent");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        send.start();
    }



}
