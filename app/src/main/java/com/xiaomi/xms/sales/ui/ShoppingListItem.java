
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.CartListNode;
import com.xiaomi.xms.sales.util.LogUtil;

public class ShoppingListItem extends BaseListItem<CartListNode> {

    private ImageView mImage;
    private TextView mTtile;
    private TextView mTextCenter;
    private ImageView mArrow;
    private ImageView mShowType;
    private View mContainer;
    private View mTopLine;
    private static String TAG = "ShoppingListItem";

    public ShoppingListItem(Context context) {
        super(context, null);
    }

    public ShoppingListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.shopping_cartlist_item, this, true);
        mTopLine = findViewById(R.id.action_divider);
        mImage = (ImageView) findViewById(R.id.shopping_cartlist_photo);
        mTtile = (TextView) findViewById(R.id.shopping_cartlist_text_title);
        mTextCenter = (TextView) findViewById(R.id.shopping_cartlist_text_center);
        mArrow = (ImageView) findViewById(R.id.arrow_right);
        mShowType = (ImageView) findViewById(R.id.showtype);
        mContainer = findViewById(R.id.container);
    }

    public View getContainer() {
        return mContainer;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImage = (ImageView) findViewById(R.id.shopping_cartlist_photo);
        mTtile = (TextView) findViewById(R.id.shopping_cartlist_text_title);
        mTextCenter = (TextView) findViewById(R.id.shopping_cartlist_text_center);
    }

    @Override
    public void bind(CartListNode data) {
        mTtile.setText(data.getTitle());
        mTextCenter.setText(String.format(
                getResources().getString(R.string.shopping_cartlist_text_center_template),
                data.getPrice(), data.getCount(), data.getTotal()));
        ImageLoader.getInstance()
                .loadImage(mImage, data.getThumbnail(), R.drawable.list_default_bg);
        LogUtil.d(TAG, " The thumbnail url is: " + data.getThumbnail());
        this.setTag(data);
        String showType = data.getShowType();
        if (TextUtils.equals(showType, Tags.ShoppingCartList.SHOWTYPE_BARGIN)) {
            mTtile.setText(String.format(
                    getResources().getString(R.string.cartitem_title_bargin_template),
                    data.getTitle()));
        } else if (TextUtils.equals(showType, Tags.ShoppingCartList.SHOWTYPE_GIFT)) {
            mShowType.setImageResource(R.drawable.cartlist_item_showtype_gift);
        } else if (TextUtils.equals(showType, Tags.ShoppingCartList.SHOWTYPE_SECKILL)) {
            mShowType.setImageResource(R.drawable.cartlist_item_showtype_seckill);
        } else if (TextUtils.equals(showType, Tags.ShoppingCartList.SHOWTYPE_SPECIAL)) {
            mShowType.setImageResource(R.drawable.cartlist_item_showtype_special);
        } else if (TextUtils.equals(showType, Tags.ShoppingCartList.SHOWTYPE_ERNIE)) {
            mShowType.setImageResource(R.drawable.cartlist_item_showtype_ernie);
        } else {
            mShowType.setImageDrawable(null);
        }
    }

    public void showTopLine(boolean isShow) {
        if (isShow) {
            mTopLine.setVisibility(View.VISIBLE);
            LayoutParams params = new LayoutParams(mImage.getLayoutParams());
            params.setMargins(30, 20, 7, 20);
            mImage.setLayoutParams(params);
        } else {
            mTopLine.setVisibility(View.GONE);
        }
    }

    public void hideArrow(boolean isHide) {
        if (isHide) {
            mArrow.setVisibility(View.GONE);
        } else {
            mArrow.setVisibility(View.VISIBLE);
        }
    }
}
