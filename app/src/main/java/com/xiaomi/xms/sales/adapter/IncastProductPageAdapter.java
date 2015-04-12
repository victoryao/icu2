
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.IncastNode.IncastProduct;

public class IncastProductPageAdapter extends BasePageAdapter<IncastProduct> {

    public IncastProductPageAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, IncastProduct data, ViewGroup parent) {
        ImageView iv = new ImageView(context);
        return iv;
    }

    @Override
    public void bindView(View view, int position, IncastProduct data) {
        ImageLoader.getInstance().loadImage((ImageView) view, data.getPhoto(),
                R.drawable.default_pic_large);
    }
}
