package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.PhoneModelInfo;

public class PhoneTypeItem extends BaseListItem<PhoneModelInfo>{

    private ImageView mTypeImg;
    private TextView mTypeText;

    public PhoneTypeItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTypeText = (TextView) findViewById(R.id.phone_type_text);
        mTypeImg = (ImageView) findViewById(R.id.phone_type_img);
    }

    @Override
    public void bind(PhoneModelInfo data) {
        mTypeText.setText(data.getPhoneName());
        ImageLoader.getInstance().loadImage(mTypeImg, data.getImage(), null);
    }

}
