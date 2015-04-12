
package com.xiaomi.xms.sales.ui;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.CategoryInfo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryListItem extends BaseListItem<CategoryInfo>{
    private ImageView mCategoryPhoto;

    private TextView mCategoryName;

    public CategoryListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCategoryPhoto = (ImageView) findViewById(R.id.category_photo);
        mCategoryName = (TextView) findViewById(R.id.category_name);
    }

    @Override
    public void bind(CategoryInfo data) {
        mCategoryName.setText(data.getName());
        ImageLoader.getInstance().loadImage(mCategoryPhoto, data.getPhoto(), R.drawable.default_pic_small_inverse);
    }

}
