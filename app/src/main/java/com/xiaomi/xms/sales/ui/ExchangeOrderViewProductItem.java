/**
 * @author
 * @since
 **/

package com.xiaomi.xms.sales.ui;

import java.math.BigDecimal;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.Order.ProductBrief;
import com.xiaomi.xms.sales.util.Utils;

public class ExchangeOrderViewProductItem extends BaseListItem<ProductBrief> {

    private ImageView mImage;
    private TextView mTitle;
    private TextView mPrice;
    private ImageView mChoice;
    private TextView mSn;

    public ExchangeOrderViewProductItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImage = (ImageView) findViewById(R.id.product_photo);
        mTitle = (TextView) findViewById(R.id.product_title);
        mChoice = (ImageView) findViewById(R.id.choice);
        mPrice = (TextView) findViewById(R.id.product_price);
        mSn = (TextView) findViewById(R.id.product_sn);
    }

    @Override
    public void bind(ProductBrief data) {
        ImageLoader.getInstance().loadImage(mImage, data.mProductImage, R.drawable.list_default_bg);
        mTitle.setText(data.mProductName);
        if (!TextUtils.isEmpty(data.mProductPrice)) {
            Double sumPrice = mul(data.mProductCount, Double.parseDouble(data.mProductPrice));
            mPrice.setText(getResources().getString(
                    R.string.order_product_center,
                    data.mProductPrice,
                    data.mProductCount,
                    Utils.Money.valueOf(sumPrice)));
            String sn = "";
            if(data.mIsMobile.equals("0")){
            	if(data.mNewSn != null){
            		setChecked(true);
            		sn = "原SN: "+data.mSn + "\n新SN: "+data.mNewSn;
            	}else{
            		setChecked(false);
            		sn = "SN: "+data.mSn;
            	}
            }else{
            	if(data.mNewImei != null){
            		setChecked(true);
            		sn = "原Imei: "+data.mImei + "\n新Imei: "+data.mNewImei;
            	}else{
            		setChecked(false);
            		sn = "IMEI: "+ data.mImei;
            	}
            }
            mSn.setText(sn);
        }
    }

    public static Double mul(int v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.multiply(b2).doubleValue();
    }
    
    public void setChecked(boolean check) {
        if (check) {
            mChoice.setBackgroundResource(R.drawable.multiple_choice_p);
        } else {
            mChoice.setBackgroundResource(R.drawable.multiple_choice_n);
        }
    }

}
