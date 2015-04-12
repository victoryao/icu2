
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.ActNode;

public class ShoppingActItem extends BaseListItem<ActNode> {

    private TextView mType;
    private TextView mTitle;

    public ShoppingActItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.shopping_act_item, this,
                true);
        mType = (TextView) findViewById(R.id.type_info);
        mTitle = (TextView) findViewById(R.id.name_info);
    }

    @Override
    public void bind(ActNode data) {
        mType.setText(data.type);
        mTitle.setText(data.info);
    }

}
