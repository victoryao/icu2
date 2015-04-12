package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.TitleNode;

public class ShoppingTitleItem extends BaseListItem<TitleNode> {
    private TextView mTitle;
    public ShoppingTitleItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.shopping_title_item, this, true);
        mTitle = (TextView) findViewById(R.id.title);
    }

    @Override
    public void bind(TitleNode data) {
        if (TextUtils.isEmpty(data.getTitle())) {
            mTitle.setVisibility(View.GONE);
        } else {
            mTitle.setText(data.getTitle());
            mTitle.setVisibility(View.VISIBLE);
        }
    }

}
