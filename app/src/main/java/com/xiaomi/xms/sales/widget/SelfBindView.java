
package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.xiaomi.xms.sales.model.Image;

public class SelfBindView extends ImageView {

    public SelfBindView(Context context) {
        super(context);
    }

    public SelfBindView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelfBindView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface SelfBindViewInteface {
        public void bindView(ImageView view ,Bitmap bitmap ,Image image);
    }

    public SelfBindViewInteface SelfBindViewCallBack;
}
