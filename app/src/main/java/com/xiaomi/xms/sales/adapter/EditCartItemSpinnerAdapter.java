
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;

public class EditCartItemSpinnerAdapter extends BaseDataAdapter<String> {

    public EditCartItemSpinnerAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, String data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.edit_cartitem_spinner_item, parent,
                false);
    }

    @Override
    public void bindView(View view, int position, String data) {
        if (view instanceof LinearLayout) {
            TextView text = (TextView) view.findViewById(R.id.text);
            text.setText(data);
        }
    }

    protected void bindBackground(View view, int position) {

    }

}
