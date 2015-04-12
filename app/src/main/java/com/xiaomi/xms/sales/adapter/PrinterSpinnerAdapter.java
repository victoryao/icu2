
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.PrinterInfoLoader.PrinterMode;
import com.xiaomi.xms.sales.ui.PrinterModeSpinnerItem;

public class PrinterSpinnerAdapter extends BaseDataAdapter<PrinterMode> {
    public PrinterSpinnerAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, PrinterMode data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.printer_mode_spinner_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, PrinterMode data) {
        if (view instanceof PrinterModeSpinnerItem) {
            ((PrinterModeSpinnerItem) view).bind(data);
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
}
