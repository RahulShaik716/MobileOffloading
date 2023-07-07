package com.dshaik3.asu.mobileoffloading;

// Singleton class
public class DeviceHolder {
    private static Device device;

    public static Device getMyDevice() {
        return device;
    }

    public static void setDevice(Device mdevice) {
         device = mdevice;
    }

    public static String ArrayToString(int[][] array)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                stringBuilder.append(array[i][j]).append(" ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
