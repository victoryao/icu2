
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.FcodeSelectProduct;
import com.xiaomi.xms.sales.ui.FCodeProductListItem;

public class FCodeSelectAdapter extends BaseDataAdapter<FcodeSelectProduct> {

    private int mCheckedPosition;

    public FCodeSelectAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, FcodeSelectProduct data, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.fcode_product_list_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, FcodeSelectProduct data) {
        if (view instanceof FCodeProductListItem) {
            FCodeProductListItem item = (FCodeProductListItem) view;
            item.bind(data);
            item.setTag(data.getProductId());
        }
    }

    public void setCheckedPosition(int position) {
        mCheckedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    protected void bindBackground(View view, int position) {
        int resourceId;
        if (getCount() == 1) {
            resourceId = R.drawable.fcode_radiobutton_single_bg;
        } else {
            if (position == 0) {
                resourceId = position == mCheckedPosition ? R.drawable.fcode_radiobutton_up_bg_p
                        : R.drawable.fcode_radiobutton_up_bg_n;
            } else if (position == getCount() - 1) {
                resourceId = position == mCheckedPosition ? R.drawable.fcode_radiobutton_bottom_bg_p
                        : R.drawable.fcode_radiobutton_bottom_bg_n;
            } else {
                resourceId = position == mCheckedPosition ? R.drawable.fcode_radiobutton_middle_bg_p
                        : R.drawable.fcode_radiobutton_middle_bg_n;
            }
        }
        view.setBackgroundResource(resourceId);
    }
}
