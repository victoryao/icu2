/**
 * @author
 * @since
 **/

package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.Order.ProductBrief;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;

import java.math.BigDecimal;

public class OrderViewProductSnItem extends BaseListItem<ProductBrief> {

    private ImageView mImage;
    private TextView mTitle;
    private TextView mPrice;
    private TextView mSn;

    public OrderViewProductSnItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImage = (ImageView) findViewById(R.id.product_photo);
        mTitle = (TextView) findViewById(R.id.product_title);
        mPrice = (TextView) findViewById(R.id.product_price);
        mSn = (TextView) findViewById(R.id.product_sn);
    }

    @Override
    public void bind(ProductBrief data) {
        LogUtil.i("OrderViewProductItem", data.mProductImage.getFileUrl());
        ImageLoader.getInstance().loadImage(mImage, data.mProductImage, R.drawable.list_default_bg);
        mTitle.setText(data.mProductName);
        if (!TextUtils.isEmpty(data.mProductPrice)) {
            Double sumPrice = mul(data.mProductCount, Double.parseDouble(data.mProductPrice));
            mPrice.setText(getResources().getString(
                    R.string.order_product_center,
                    data.mProductPrice,
                    data.mProductCount,
                    Utils.Money.valueOf(sumPrice)));
        }
        String sn = "";
        if(data.mIsMobile.equals("0")){
        	sn = "SN: "+data.mSn;
        }else{
        	sn = "IMEI: "+ data.mImei;
        }
        mSn.setText(sn);
        
    }

    public static Double mul(int v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.multiply(b2).doubleValue();
    }

}
