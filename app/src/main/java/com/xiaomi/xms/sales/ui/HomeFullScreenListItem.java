
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.HomeInfo;

public class HomeFullScreenListItem extends HomeBaseListItem {
    private static final String TAG = "HomeFullScreenListItem";
    private ImageView mHomePhoto;
    private ImageView mActivityTypePhoto;

    public HomeFullScreenListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHomePhoto = (ImageView) findViewById(R.id.home_photo);
        mActivityTypePhoto = (ImageView) findViewById(R.id.activity_type_photo);
    }

    @Override
    public void bind(HomeInfo data) {
        Bitmap defaultBitmap = ImageLoader.getInstance().syncLoadLocalImage(data.getHomeThumbnail(), false);
        if (defaultBitmap == null) {
            ImageLoader.getInstance().loadImage(mHomePhoto, data.getHomePhoto(),
                    R.drawable.default_pic_small_inverse);
        } else {
            ImageLoader.getInstance().loadImage(mHomePhoto, data.getHomePhoto(),
                    defaultBitmap);
        }
        ImageLoader.getInstance().loadImage(mActivityTypePhoto, data.getActivityIcon(), 0);
    }
}
