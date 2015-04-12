
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.FcodeSelectProduct;

public class FCodeProductListItem extends BaseListItem<FcodeSelectProduct> {

    private TextView mProductName;
    private ImageView mProductPhoto;
    private TextView mPrice;

    public FCodeProductListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProductName = (TextView) findViewById(R.id.product_title);
        mProductPhoto = (ImageView) findViewById(R.id.product_photo);
        mPrice = (TextView) findViewById(R.id.price);
    }

    @Override
    public void bind(FcodeSelectProduct data) {
        mProductName.setText(data.getName());
        mPrice.setText(getResources().getString(R.string.rmb_identification, data.getPrice()));
        ImageLoader.getInstance().loadImage(mProductPhoto, data.getImage(),
                R.drawable.default_pic_small);
    }

}
