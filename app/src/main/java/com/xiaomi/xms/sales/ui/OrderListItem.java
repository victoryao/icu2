/**
 * @author
 * @since
**/

package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.OrderPreview;
import com.xiaomi.xms.sales.util.Device;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderListItem extends BaseListItem<OrderPreview> {

    private static final int MILLSECONDS = 1000;

    private ImageView mImageView;
    private TextView mTimeView;
    private TextView mSecondView;
    private TextView mThirdFirstView;
    private TextView mThirdSecondView;
    private TextView mDeliverInfo;
    private TextView mDeliverInfoM1;

    private Context mContext;
    public OrderListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.order_list_item_photo);
        mTimeView = (TextView) findViewById(R.id.order_list_item_time);
        mDeliverInfo = (TextView) findViewById(R.id.order_list_deliverinfo);
        mSecondView = (TextView) findViewById(R.id.order_list_item_second);
        mThirdFirstView = (TextView) findViewById(R.id.order_list_item_third_first);
        mThirdSecondView = (TextView) findViewById(R.id.order_list_item_third_second);
        mDeliverInfoM1 = (TextView) findViewById(R.id.order_list_deliverinfo_m1);
    }

    @Override
    public void bind(OrderPreview data) {
        ImageLoader.getInstance()
                .loadImage(mImageView, data.getImage(), R.drawable.list_default_bg);
        SimpleDateFormat sdf = new SimpleDateFormat(
                mContext.getString(R.string.order_list_time_format));
        mTimeView.setText(sdf.format(new Date(Long.parseLong(data.getAddTime()))));
        if (Device.DISPLAY_WIDTH <= 480) {
            if (data.getDeliverCount() > 1) {
                mDeliverInfoM1.setText(mContext.getString(R.string.order_deliver_info,
                        data.getDeliverCount()));
                mDeliverInfoM1.setVisibility(View.VISIBLE);
            } else {
                mDeliverInfoM1.setVisibility(View.GONE);
            }
        } else {
            mDeliverInfoM1.setVisibility(View.GONE);
            if (data.getDeliverCount() > 1) {
                mDeliverInfo.setText(mContext.getString(R.string.order_deliver_info,
                        data.getDeliverCount()));
                mDeliverInfo.setVisibility(View.VISIBLE);
            } else {
                mDeliverInfo.setVisibility(View.GONE);
            }
        }

        mSecondView.setText(mContext.getString(R.string.order_list_fee)
                + mContext.getString(R.string.order_list_rmb) + data.getFee());
        mThirdFirstView.setText(R.string.order_list_status);
        mThirdSecondView.setText(data.getOrderStatusString());
    }
}
