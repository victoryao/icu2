
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.OrderListActivity;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.Order.DeliverOrder;
import com.xiaomi.xms.sales.model.Order.OrderExpress;
import com.xiaomi.xms.sales.model.Order.ProductBrief;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;

import java.util.ArrayList;

public class OrderViewDeliverItem extends BaseListItem<DeliverOrder> {

    private TextView mDeliverIdView;
    private TextView mDeliverStatusView;
    private TextView mDeliverTitle;
    private View mExpressView;
    private LinearLayout mProductView;
    private Context mContext;

    private String mExpressId;

    public OrderViewDeliverItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDeliverIdView = (TextView) findViewById(R.id.deliver_id);
        mDeliverStatusView = (TextView) findViewById(R.id.order_deliver_status);
        mDeliverTitle = (TextView) findViewById(R.id.item_title);
        mExpressView = findViewById(R.id.order_express_info);
        mProductView = (LinearLayout) findViewById(R.id.prodct_container);
        mExpressView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderListActivity activity = (OrderListActivity) mContext;
                Bundle bundle = new Bundle();
                bundle.putString(Constants.Intent.EXTRA_ORDER_EXPRESS, mExpressId);
                activity.showFragment(OrderListActivity.TAG_ORDER_EXPRESS, bundle, true);
            }
        });
    }

    @Override
    public void bind(DeliverOrder data) {
        mDeliverIdView.setText(mContext.getString(R.string.order_deliver_id, data.mDeliverId));
        mDeliverStatusView.setText(mContext.getString(R.string.order_deliver_status,
                data.mOrderStatusInfo));
        mExpressId = data.mDeliverId;

        mDeliverTitle.setText(mContext.getString(R.string.deliver_title_info,
                data.mDeliverId.substring(data.mDeliverId.length() - 1, data.mDeliverId.length())));
        // express
        OrderExpress express = data.mDeliveExpress;
        if (express != null && !TextUtils.isEmpty(express.mExpressName)
                && !TextUtils.isEmpty(express.mExpressSN)) {
            mExpressView.setVisibility(View.VISIBLE);
            TextView text = (TextView) mExpressView.findViewById(R.id.order_express_text);
            text.setText(express.mExpressName);
            TextView snText = (TextView) mExpressView.findViewById(R.id.order_express_sn);
            snText.setText(express.mExpressSN);
            ImageView image = (ImageView) mExpressView.findViewById(R.id.arrow_right);
            mExpressView.setEnabled(true);
            image.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(express.mExpressSN)) {
                mExpressView.setEnabled(false);
                image.setVisibility(View.GONE);
            }
        }
        mProductView.removeAllViewsInLayout();
        ArrayList<ProductBrief> products = data.mDeliveProducts;
        if (products != null && products.size() > 0) {
            for (int i = 0; i < products.size(); i++) {
                View productViewItem = LayoutInflater.from(mContext).inflate(
                        R.layout.order_view_product_item, null);
                productViewItem.setPadding(0, 0, 0, 0);
                productViewItem.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        Device.DISPLAY_DENSITY * 80 / 160));
                productViewItem.setBackgroundDrawable(null);
                ImageView image = (ImageView) productViewItem.findViewById(R.id.product_photo);
                image.setPadding(0, 0, 0, 0);
                TextView title = (TextView) productViewItem.findViewById(R.id.product_title);
                TextView price = (TextView) productViewItem.findViewById(R.id.product_price);
                ImageLoader.getInstance().loadImage(image, products.get(i).mProductImage,
                        R.drawable.list_default_bg);
                title.setText(products.get(i).mProductName);
                price.setText(getResources().getString(
                        R.string.order_product_center,
                        products.get(i).mProductPrice,
                        products.get(i).mProductCount,
                        products.get(i).mTotalPrice));
                mProductView.addView(productViewItem);
            }
        }

    }

}
