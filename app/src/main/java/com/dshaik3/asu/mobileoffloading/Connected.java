package com.dshaik3.asu.mobileoffloading;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dshaik3.asu.mobileoffloading.master.Master;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

public class Connected extends AppCompatActivity {
private Device device = DeviceHolder.getMyDevice();
private BluetoothDevice Bdevice = device.getDevice();


private static final java.util.UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID

private BluetoothSocket socket = null;
private OutputStream outputStream = null;

private InputStream inputStream = null;

private   ObjectInputStream objectInputStream = null;
private   ObjectOutputStream objectOutputStream = null;
private     int [][] result  = new int[2][2];

private TextView MatrixResult;
private TextView M_exec_time;
private TextView S_exec_time;

private TextView Battery;
private TextView Location;

private TextView LogText;
long slave_exec ;
long exec_time;
//3 threads
    private  Thread ConnectThread;
    private Thread SendThread;
    private Thread MonitorThread;
    private Thread ComputeThread;

    private Button Connect;
    private Button Monitor;
    private Button SendData;
    private boolean isRunning = true;
@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_connected);

TextView Name = (TextView) findViewById(R.id.textView4);
TextView Address = (TextView) findViewById(R.id.textView5);
Connect = (Button) findViewById(R.id.button2);
Button BDisconnect = (Button)findViewById(R.id.button3);
SendData = (Button)findViewById(R.id.button4);
MatrixResult = findViewById(R.id.textView6);
M_exec_time = findViewById(R.id.textView10);
S_exec_time = findViewById(R.id.textView11);
Monitor = findViewById(R.id.button6);
Battery = findViewById(R.id.textView12);
Location = findViewById(R.id.textView13);
LogText = findViewById(R.id.textView14);
LogText.setMovementMethod(new ScrollingMovementMethod());
Name.setText(device.getName());
Address.setText(device.getAddress());

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
connect();
BDisconnect.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
Disconnect();
}
});

SendData.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
matrixmultiplication();
}
});

Monitor.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        HashMap data = new HashMap<>();
        data.put("request",true);
        sendDataToSlave(data);
        startListening();
        Monitor.setEnabled(false);
    }
});

}

private void startListening()
{
    Thread listen = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (isRunning && socket!=null) {
                    if (inputStream == null && socket!=null) {
                        inputStream = socket.getInputStream();
                        objectInputStream = new ObjectInputStream(inputStream);
                    }
                    if(objectInputStream.available() != -1 && socket!=null)
                    {
                    HashMap received = (HashMap) objectInputStream.readObject();
                    if (received.containsKey("battery")) {
                        String location = (String) received.get("location");
                        int battery = (int) received.get("battery");
                        writeLogToFile(device.getName());
                        writeLogToFile("battery percentage :" + battery + "%");
                        writeLogToFile("Location of the slave is : " + location);
                        writeLogToFile("Timestamp:"+new Date(System.currentTimeMillis()));
                        writeLogToFile("\n");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Battery.setText("battery percentage :" + battery + "%");
                                Location.setText("Location of the slave is : " + location);
                                LogText.setText(readLogFile());
                            }
                        });
                    }
                    if (received.containsKey("row2")) {
                        result[1] = (int[]) received.get("row2");
                        slave_exec = (long) received.get("slave_exe");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MatrixResult.setText(DeviceHolder.ArrayToString(result));
                                M_exec_time.setText(exec_time + "  nano seconds");
                                S_exec_time.setText(slave_exec + " nano seconds");
                            }
                        });

                    }   }
                }
                }
            catch(IOException e){
                     if(!socket.isConnected())
                     {
                       socket = null;
                       Intent intent = new Intent(Connected.this, Master.class);
                       startActivity(intent);
                     }
                     else
                    throw new RuntimeException(e);
                } catch(ClassNotFoundException e){
                    throw new RuntimeException(e);
                }
        }
    });

    listen.start();

}

private void connect()
{
 ConnectThread = new Thread(new Runnable() {
     @SuppressLint("MissingPermission")
     @Override
     public void run() {
         try {
             socket = Bdevice.createRfcommSocketToServiceRecord(UUID);
             socket.connect();
             Log.i("connected","woo");
         } catch (IOException e) {
             throw new RuntimeException(e);
         }

     }
 });
 ConnectThread.start();
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            Connect.setEnabled(false);
        }
    });
}

private void sendDataToSlave(HashMap data)  {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                        if(outputStream==null){
                            outputStream = socket.getOutputStream();
                        objectOutputStream = new ObjectOutputStream(outputStream);}
                        objectOutputStream.writeObject(data);
                        objectOutputStream.flush();
                        Log.i("master","sent");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
}

@Override
protected void onDestroy() {
super.onDestroy();
OutputStreamClose();
inputStreamClose();
socketClose();
Disconnect();
}

@SuppressLint("MissingPermission")
private void Disconnect()
{
if(Bdevice!=null)
{
if(Bdevice.getBondState()==BluetoothDevice.BOND_BONDED)
{
    try {
        Method method = Bdevice.getClass().getMethod("removeBond");
        method.invoke(Bdevice);
        if(socket!=null)
        socket.close();
        socket = null;
    } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
}
}
private void OutputStreamClose()
{

if(outputStream!=null) {
    try {
        objectOutputStream.close();
        outputStream.close();

    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
}

private  void socketClose() {
if(socket!=null) {
    try {
        socket.close();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
}
private void inputStreamClose() {
if(inputStream!=null)
{
try {
    objectInputStream.close();
inputStream.close();


} catch (IOException e) {
throw new RuntimeException(e);
}
}
}

private void matrixmultiplication()
{
int[][] matrix1 = {{1,2},{2,3}};
int[][] matrix2 = {{3,4},{5,6}};


//send row 2 and matrix2 to slave device
HashMap keyValuePair = new HashMap<>();
keyValuePair.put("row2",matrix1[1]);
keyValuePair.put("matrix2",matrix2);


sendDataToSlave(keyValuePair);

//compute row1 in master ..
long start_time = System.nanoTime();
for(int i=0; i<2;i++)
{
    int sum = 0 ;
    for(int j=0;j<2;j++)
    {
        sum+= matrix1[0][j]*matrix2[j][i];
    }
    result[0][i] = sum;
}
long end_time  = System.nanoTime();

   Log.i("row1",DeviceHolder.ArrayToString(result));

    exec_time = end_time - start_time;

}
    private void writeLogToFile(String logMessage) {
        // Get the directory for your app's private files
        File directory = getFilesDir();

// Create the file object
        File logFile = new File(directory, device.getName()+".txt");
        try {
            FileWriter fileWriter = new FileWriter(logFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(logMessage);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readLogFile() {
        // Get the directory for your app's private files
        File directory = getFilesDir();

// Create the file object
        File logFile = new File(directory, device.getName()+".txt");// Replace with the actual path to your log file

        StringBuilder logContent = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line;
            while ((line = reader.readLine()) != null) {
                logContent.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logContent.toString();
    }

}