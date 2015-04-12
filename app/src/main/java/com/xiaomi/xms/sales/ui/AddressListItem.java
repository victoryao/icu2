
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.AddressInfo;
import com.xiaomi.xms.sales.util.Utils.PhoneFormat;

public class AddressListItem extends BaseListItem<AddressInfo> {

    private TextView mConsignee;
    private TextView mArea;
    private TextView mAddress;

    public AddressListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mConsignee = (TextView) findViewById(R.id.address_consignee);
        mArea = (TextView) findViewById(R.id.address_area);
        mAddress = (TextView) findViewById(R.id.address);
    }

    @Override
    public void bind(AddressInfo data) {
        Context context = this.getContext();
        String tel = data.getTel();
        mConsignee.setText(context.getString(R.string.address_title, data.getConsignee(),
                PhoneFormat.valueOf(tel)));
        mArea.setText(context.getString(R.string.address_area, data.getProvince(), data.getCity(),
                data.getDistrict()));
        mAddress.setText(context.getString(R.string.address_location, data.getAddress(),
                data.getZipCode()));
    }

}
