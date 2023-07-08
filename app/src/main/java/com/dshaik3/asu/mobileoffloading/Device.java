package com.dshaik3.asu.mobileoffloading;

import android.bluetooth.BluetoothDevice;


import androidx.annotation.Nullable;

public class Device {
    private String Name;
    private String Address;
    private BluetoothDevice BDevice;

    public Device(@Nullable String name, String address,BluetoothDevice BDevice)
    {
        if(name == null)
            this.Name = "No Name";
        else
            this.Name = name;

        this.Address = address;
        this.BDevice = BDevice;

    }


    public String getName() {
        return this.Name;
    }

    public String getAddress() {
        return this.Address;
    }
    public BluetoothDevice getDevice()
    {
        return this.BDevice;
    }
}
