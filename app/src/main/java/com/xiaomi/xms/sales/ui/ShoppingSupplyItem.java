
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.SupplyNode;

public class ShoppingSupplyItem extends BaseListItem<SupplyNode> {
    private TextView mTitle;
    private ImageView mChoice;
    private ImageView mShow;

    public ShoppingSupplyItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.shopping_supply_item, this, true);
        mTitle = (TextView) findViewById(R.id.title);
        mChoice = (ImageView) findViewById(R.id.choice);
        mShow = (ImageView) findViewById(R.id.arrow_right);
        mShow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShoppingActivity activity = (ShoppingActivity) getContext();
                Bundle bundle = new Bundle();
                SupplyNode node = (SupplyNode) getTag();
                bundle.putString(Tags.Product.PRODUCT_ID, node.getProductId());
                if (node.getCheckedStatus()) {
                    bundle.putString(Tags.ShoppingSupply.BOUGHT_PRODUCT_ID, node.getProductId());
                }
                if (node.getSelectableProducts() != null) {
                    bundle.putParcelableArrayList(Tags.ShoppingSupply.SELECTABLE_PRODUCTS,
                            node.getSelectableProducts());
                }
                bundle.putString(Tags.ShoppingSupply.ITEM_ID, node.getItemId());
                bundle.putString(Tags.ShoppingSupply.ACT_ID, node.getActId());
                activity.showFragment(ShoppingActivity.Fragments.TAG_SHOPPING_PRODUCT_FRAGMENT,
                        bundle, true);
            }
        });
    }

    @Override
    public void bind(SupplyNode data) {
        setTag(data);
        mTitle.setText(data.getBargainName());
        setChecked(data.getCheckedStatus());
    }

    public void setChecked(boolean check) {
        if (check) {
            mChoice.setBackgroundResource(R.drawable.multiple_choice_p);
        } else {
            mChoice.setBackgroundResource(R.drawable.multiple_choice_n);
        }
    }

    public void setArrowBackgroundResource(int id) {
        mShow.setBackgroundResource(id);
    }
}
