package com.dshaik3.asu.mobileoffloading;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dshaik3.asu.mobileoffloading.master.Master;

import java.util.List;

public class ConnectedDeviceAdapter extends ArrayAdapter<Device> {
    private Context mContext;
    private int mLayout;
    private List<Device>mList;

    // View lookup cache
    private static class ViewHolder {
        TextView Name;
        TextView Address;

    }
    public ConnectedDeviceAdapter(Context context, int resource, List<Device> mDevices) {
        super(context, resource,mDevices);
        this.mContext = context;
        this.mLayout = resource;
        this.mList = mDevices;
    }
    public void addDevices(List<Device> mDevices)
    {
        this.mList = mDevices;
        notifyDataSetChanged();
    }
    @SuppressLint("MissingPermission")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Device device = getItem(position);
        final View result;
        ViewHolder viewHolder;
        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(mLayout, parent, false);
            viewHolder.Name = (TextView) convertView.findViewById(R.id.textView2);
            viewHolder.Address = (TextView) convertView.findViewById(R.id.textView3);
            result = convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        viewHolder.Name.setText(device.getName());
        viewHolder.Address.setText(device.getAddress());
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("adapter","clicked");
                ((Master)mContext).Connect(device);
            }
        });
        return result;
    }
}
