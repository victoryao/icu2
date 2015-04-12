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
import com.xiaomi.xms.sales.model.ProductInfo;

public class ProductListItem extends BaseGridItem<ProductInfo>{

    private ImageView mProductPhoto;
    private TextView mProductName;
    private TextView mProductPrice;
    private TextView mMarketPrice;
    private ImageView mProductPhotoLabel;

    public ProductListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void bind(ProductInfo data) {
        mProductName.setText(data.getProductName());
        mProductPrice.setText(getResources().getString(R.string.rmb_identification,
                data.getProductPrice()));
        ImageLoader.getInstance().loadImage(mProductPhoto, data.getImage(),
                R.drawable.default_pic_small);
        if (data.hasProduct()) {
            mMarketPrice.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
            mMarketPrice.setText(getResources().getString(R.string.rmb_identification,
                    data.getMarketPrice()));
            mProductPrice.setTextColor(getResources()
                    .getColor(R.color.highlight_text_color));
            if (TextUtils.equals(data.getProductPrice(), data.getMarketPrice())) {
                mMarketPrice.setVisibility(View.GONE);
                mProductPhotoLabel.setVisibility(View.GONE);
            } else {
                mMarketPrice.setVisibility(View.VISIBLE);
                mProductPhotoLabel.setVisibility(View.VISIBLE);
            }
        } else {
            mMarketPrice.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            mMarketPrice.setText(getResources().getString(R.string.none_stock));
            mProductPrice.setTextColor(getResources()
                    .getColor(R.color.secondary_text_color_inverse));
            mMarketPrice.setVisibility(View.VISIBLE);
            mProductPhotoLabel.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProductName = (TextView) findViewById(R.id.product_name);
        mProductPhoto = (ImageView) findViewById(R.id.product_photo);
        mProductPhotoLabel = (ImageView) findViewById(R.id.product_photo_label);
        mProductPrice = (TextView) findViewById(R.id.product_price);
        mMarketPrice = (TextView) findViewById(R.id.marketPrice);
    }
}
