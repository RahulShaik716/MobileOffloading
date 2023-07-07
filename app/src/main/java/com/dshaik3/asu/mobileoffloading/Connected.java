package com.dshaik3.asu.mobileoffloading;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dshaik3.asu.mobileoffloading.master.Master;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Connected extends AppCompatActivity {
private Device device = DeviceHolder.getMyDevice();
private BluetoothDevice Bdevice = device.getDevice();


private static final java.util.UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID

private BluetoothSocket socket = null;
private OutputStream outputStream = null;

private InputStream inputStream = null;

private ObjectInputStream objectInputStream;
private     int [][] result  = new int[2][2];

private TextView MatrixResult;
private TextView M_exec_time;
private TextView S_exec_time;

private TextView Battery;
private TextView Location;
long slave_exec ;
@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_connected);

TextView Name = (TextView) findViewById(R.id.textView4);
TextView Address = (TextView) findViewById(R.id.textView5);
Button Connect = (Button) findViewById(R.id.button2);
Button BDisconnect = (Button)findViewById(R.id.button3);
Button SendData = (Button)findViewById(R.id.button4);
MatrixResult = findViewById(R.id.textView6);
M_exec_time = findViewById(R.id.textView10);
S_exec_time = findViewById(R.id.textView11);
Button Monitor = findViewById(R.id.button6);
Battery = findViewById(R.id.textView12);
Location = findViewById(R.id.textView13);

Name.setText(device.getName());
Address.setText(device.getAddress());

Connect.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
new Thread(new ConnectThread()).start();
}
});
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
    }
});

}

private void startListening()
{
    Thread listen = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                if(inputStream!=null)
                    inputStream.close();


                inputStream = socket.getInputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                //need to have a condition
                HashMap received = (HashMap) objectInputStream.readObject();
                    String location = (String) received.get("location");
                    int battery = (int) received.get("battery");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Battery.setText("battery percentage :"+battery+"%");
                            Location.setText("Location of the slave is : "+location);
                        }
                    });

            }
            catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        });
    listen.start();
}
private class  ConnectThread implements Runnable{

@SuppressLint("MissingPermission")
@Override
public void run() {

try {
 socket = device.getDevice().createRfcommSocketToServiceRecord(UUID);
 if(Bdevice.getBondState() != BluetoothDevice.BOND_BONDED)
 {
     Method method = Bdevice.getClass().getMethod("createBond");
     method.invoke(Bdevice);
 }
 socket.connect();
} catch (IOException e) {
 throw new RuntimeException(e);
} catch (InvocationTargetException e) {
 throw new RuntimeException(e);
} catch (NoSuchMethodException e) {
 throw new RuntimeException(e);
} catch (IllegalAccessException e) {
 throw new RuntimeException(e);
}
}
}

private void sendDataToSlave(HashMap data)
{
try {

 outputStream = socket.getOutputStream();
 ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
 objectOutputStream.writeObject(data);
 objectOutputStream.flush();

} catch (IOException e) {
throw new RuntimeException(e);
}
}

@Override
protected void onDestroy() {
super.onDestroy();
OutputStreamClose();
socketClose();
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
long start_time = System.currentTimeMillis();
for(int i=0; i<2;i++)
{
    int sum = 0 ;
    for(int j=0;j<2;j++)
    {
        sum+= matrix1[0][j]*matrix2[j][i];
    }
    result[0][i] = sum;
}
long end_time  = System.currentTimeMillis();

   Log.i("row1",DeviceHolder.ArrayToString(result));

    Thread waiting = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                inputStreamClose();
                inputStream = socket.getInputStream();
                objectInputStream = new ObjectInputStream(inputStream);

                while (inputStream.available()==0){
                    Thread.sleep(100);
                }
                HashMap<String, Object> receivedKeyValuePairs = (HashMap<String, Object>) objectInputStream.readObject();
                result[1] = (int[]) receivedKeyValuePairs.get("row2");
                slave_exec = (long) receivedKeyValuePairs.get("slave_exe");


            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }
    });
    waiting.start();
    try {
        waiting.join();
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    long exec_time = end_time - start_time;
    MatrixResult.setText(DeviceHolder.ArrayToString(result));
    M_exec_time.setText(exec_time+"ms");
    S_exec_time.setText(slave_exec+"ms");
}



}