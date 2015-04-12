/**
 * @author
 * @since
**/

package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.db.DBContract.Region;

public class RegionAdapter extends CursorAdapter {

    public RegionAdapter(Context context, Cursor c) {
        super(context, c, true);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        view.setTag(cursor.getInt(cursor.getColumnIndex(Region._ID)));
        TextView item = (TextView) view.findViewById(R.id.spinner_text_item);
        item.setText(cursor.getString(cursor.getColumnIndex(Region.NAME)));
    }

    @Override
    public View newView(Context context, Cursor data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.base_spinner_item, parent, false);
    }

}
