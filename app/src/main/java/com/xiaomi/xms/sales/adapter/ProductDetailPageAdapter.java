package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.ProductDetailsInfoItem;
import com.xiaomi.xms.sales.ui.ProductDetailsFullScreenItem;
import com.xiaomi.xms.sales.widget.gallery.ZoomImageView;
import com.xiaomi.xms.sales.widget.gallery.ZoomImageView.OnImageTapListener;

public class ProductDetailPageAdapter extends BasePageAdapter<ProductDetailsInfoItem> {
    private OnPageItemClickListener mPageItemClickListener;

    public interface OnPageItemClickListener {
        public void onClick();
    }

    public void setOnPageItemClickListener(OnPageItemClickListener l) {
        mPageItemClickListener = l;
    }

    public ProductDetailPageAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, ProductDetailsInfoItem data, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_details_full_screen_item,
                parent, false);
        ((ZoomImageView) view.findViewById(R.id.product_details_photo_frame))
                .setOnTapListener(new OnImageTapListener() {
                    @Override
                    public void onImageTap() {
                        if (mPageItemClickListener != null) {
                            mPageItemClickListener.onClick();
                        }
                    }
                });
        return view;
    }

    @Override
    public void bindView(View view, int position, ProductDetailsInfoItem data) {
        if (view instanceof ProductDetailsFullScreenItem) {
            ((ProductDetailsFullScreenItem) view).bind(data);
        }
    }
}
