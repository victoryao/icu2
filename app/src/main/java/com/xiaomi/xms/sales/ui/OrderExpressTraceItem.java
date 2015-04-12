/**
 * @author
 * @since
**/

package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.Order.OrderExpressTrace;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils;

public class OrderExpressTraceItem extends BaseListItem<OrderExpressTrace> {

    public OrderExpressTraceItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private ImageView mImage;
    private TextView mText;
    private TextView mTime;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImage = (ImageView) findViewById(R.id.order_list_item_photo);
        mText = (TextView) findViewById(R.id.trace_text);
        mTime = (TextView) findViewById(R.id.trace_time);
    }

    public void bind(OrderExpressTrace data, int position) {
        mText.setText(data.mText);
        if (TextUtils.equals(data.mType, Constants.OrderExpressType.ORDER_EXPRESS_LIST_TYPE_DEFAULT)) {
            mTime.setText(Utils.DateTime.formatTime(getContext(), data.mTime));
            if (position == 0) {
                mText.setTextColor(getResources().getColor(R.color.highlight_text_color));
                mTime.setTextColor(getResources().getColor(R.color.highlight_text_color));
            } else {
                mText.setTextColor(getResources().getColor(R.color.primary_text));
                mTime.setTextColor(getResources().getColor(R.color.secondary_text));
                mTime.setTextSize(14);
            }
        } else if(TextUtils.equals(data.mType, Constants.OrderExpressType.ORDER_EXPRESS_LIST_TYPE_HEAD)) {
            mTime.setText(data.mTime);
            mTime.setTextSize(16);
            if (position == 0) {
                mText.setTextColor(getResources().getColor(R.color.highlight_text_color));
                mTime.setTextColor(getResources().getColor(R.color.highlight_text_color));
            } else {
                mText.setTextColor(getResources().getColor(R.color.primary_text));
                mTime.setTextColor(getResources().getColor(R.color.primary_text));
            }
        }
    }

    @Override
    public void bind(OrderExpressTrace data) {
    }

}
