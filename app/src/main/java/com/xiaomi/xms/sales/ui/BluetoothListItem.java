
package com.xiaomi.xms.sales.ui;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;

public class BluetoothListItem extends BaseListItem<BluetoothDevice> {
    private TextView mBluetoothName;

    public BluetoothListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBluetoothName = (TextView) findViewById(R.id.bluetooth_name_text);
    }

    @Override
    public void bind(BluetoothDevice data) {
        mBluetoothName.setText(data.getName());
    }

}
