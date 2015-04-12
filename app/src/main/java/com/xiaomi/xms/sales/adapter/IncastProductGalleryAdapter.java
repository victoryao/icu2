
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.IncastNode.IncastProduct;

public class IncastProductGalleryAdapter extends BaseDataAdapter<IncastProduct> {

    private int mSelected = -1;

    public IncastProductGalleryAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, IncastProduct data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.incast_product_gallery_item, null);
    }

    @Override
    public void bindView(View view, int position, IncastProduct data) {
        ImageView iv = (ImageView) view.findViewById(R.id.img);
        ImageLoader.getInstance().loadImage(iv, data.getThumbnail(), R.drawable.default_pic_small);
        View cover = view.findViewById(R.id.cover);
        cover.setVisibility(position == mSelected ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void bindBackground(View view, int position) {
    }

    public void setSelected(int selected) {
        mSelected = selected;
    }

    public int getSelected() {
        return mSelected;
    }

}
