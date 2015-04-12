
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.ProductDetailsInfoItem;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.widget.SelfBindView;
import com.xiaomi.xms.sales.widget.SelfBindView.SelfBindViewInteface;

public class ProductDetailsItem extends BaseListItem<ProductDetailsInfoItem> {

    private SelfBindView mProductDetailPhoto;

    public ProductDetailsItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProductDetailPhoto = (SelfBindView) findViewById(R.id.product_details_photo);
    }

    @Override
    public void bind(ProductDetailsInfoItem data) {
        final LinearLayout parentView = (LinearLayout) findViewById(R.id.container);
        mProductDetailPhoto.SelfBindViewCallBack = new SelfBindViewInteface() {
            @Override
            public void bindView(ImageView view, Bitmap bitmap, Image image) {
                parentView.setLayoutParams(new LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, bitmap.getHeight()
                                * Device.DISPLAY_WIDTH / bitmap.getWidth()));
                view.setImageBitmap(image.proccessImage(bitmap));
            }
        };
        ImageLoader.getInstance().loadImage(mProductDetailPhoto, data.getImage(),
                R.drawable.default_pic_large);
    }
}
