
package com.xiaomi.xms.sales.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.BluetoothListItem;

public class BluetoothListAdapter extends BaseDataAdapter<BluetoothDevice> {

    public BluetoothListAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, BluetoothDevice data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.bluetooth_list_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, BluetoothDevice data) {
        if (view instanceof BluetoothListItem) {
            ((BluetoothListItem) view).bind(data);
            view.setTag(data);
        }
    }

}
