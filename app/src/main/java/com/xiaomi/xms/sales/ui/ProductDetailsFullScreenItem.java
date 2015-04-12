
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.ProductDetailsInfoItem;
import com.xiaomi.xms.sales.widget.gallery.ZoomImageView;

public class ProductDetailsFullScreenItem extends BaseListItem<ProductDetailsInfoItem> {
    private static final String TAG = "ProductDetailsFullScreenItem";
    private ZoomImageView mZoomImageView;

    public ProductDetailsFullScreenItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mZoomImageView = (ZoomImageView) findViewById(R.id.product_details_photo_frame);
    }

    @Override
    public void bind(ProductDetailsInfoItem data) {
        ImageLoader.getInstance().loadImage(mZoomImageView, data.getImage(),
                R.drawable.default_pic_small_inverse);
    }
}
