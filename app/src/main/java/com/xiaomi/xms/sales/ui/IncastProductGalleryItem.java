
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.IncastNode;

public class IncastProductGalleryItem extends BaseListItem<IncastNode> {

    private TextView mTitle;

    public IncastProductGalleryItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.shopping_incast_item, this, true);
        mTitle = (TextView) findViewById(R.id.name_info);
    }

    @Override
    public void bind(IncastNode data) {
        mTitle.setText(ShopApp.getContext().getString(R.string.incast_balance_template, data.getBalance()));
    }

}
