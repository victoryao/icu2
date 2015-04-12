
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.HomeInfo;

public class HomeListItem extends BaseListItem<HomeInfo> {
    private static final String TAG = "HomeListItem";
    private ImageView mHomePhoto;
    private ImageView mActivityTypePhoto;
    private TextView mProductName;
    private TextView mProductDetail;
    private TextView mProductPrice;
    private TextView mFullPrice;

    public HomeListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHomePhoto = (ImageView) findViewById(R.id.home_photo);
        mActivityTypePhoto = (ImageView) findViewById(R.id.activity_type_photo);
        mProductName = (TextView) findViewById(R.id.product_name);
        mProductDetail = (TextView) findViewById(R.id.product_detial);
        mProductPrice = (TextView) findViewById(R.id.product_price);
        mFullPrice = (TextView) findViewById(R.id.full_price);
        mFullPrice.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public void bind(HomeInfo data) {
        ImageLoader.getInstance().loadImage(mHomePhoto, data.getHomeThumbnail(),
                R.drawable.default_pic_small_inverse);
        if (null == data.getActivityIcon()) {
            mActivityTypePhoto.setImageResource(0);
            mActivityTypePhoto.setVisibility(View.GONE);
        } else {
            mActivityTypePhoto.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().loadImage(mActivityTypePhoto, data.getActivityIcon(), 0);
        }
        setTag(data);
        mProductName.setText(data.getProductName());
        mProductDetail.setText(data.getProductDetail());
        mProductPrice.setText(getContext().getString(R.string.home_product_price_format, data.getProductPrice()));
        mFullPrice.setText(getContext().getString(R.string.home_product_price_format, data.getFullPrice()));

        if (TextUtils.isEmpty(data.getProductPrice())) {
            mProductPrice.setVisibility(GONE);
        } else {
            mProductPrice.setVisibility(VISIBLE);
        }

        if (TextUtils.isEmpty(data.getFullPrice())
                || TextUtils.equals(data.getFullPrice(), data.getProductPrice())) {
            mFullPrice.setVisibility(GONE);
        } else {
            mFullPrice.setVisibility(VISIBLE);
        }
    }
}
