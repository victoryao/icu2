
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.AddressInfo;
import com.xiaomi.xms.sales.ui.AddressListItem;

public class AddressInfoAdapter extends BaseDataAdapter<AddressInfo> {

    private String mCheckedAddressId;
    private static final int TYPE_USERLIST = 0;
    private static final int TYPE_EDITLIST = 1;
    private static final int TYPE_MAX_COUNT = TYPE_EDITLIST + 1;

    private int mListType;

    public AddressInfoAdapter(Context context) {
        super(context);
        mListType = TYPE_EDITLIST;
    }

    public AddressInfoAdapter(Context context, String addressId) {
        super(context);
        mCheckedAddressId = addressId;
        mListType = TYPE_USERLIST;
    }

    @Override
    public View newView(Context context, AddressInfo data, ViewGroup parent) {
        int resourceId;
        if (mListType == TYPE_EDITLIST) {
            resourceId = R.layout.address_list_item;
        } else {
            resourceId = R.layout.address_list_item_secondly;
        }
        return LayoutInflater.from(context).inflate(resourceId, parent, false);
    }

    @Override
    public void bindView(View view, int position, AddressInfo data) {
        // 如果是选择购物车选择地址列表，设置为radio背景
        if (mListType == TYPE_USERLIST) {
            // 如果能匹配通过intent传递的addressid，设置为radio 选中状态背景
            if (mCheckedAddressId != null
                    && TextUtils.equals(data.getAddressId(), mCheckedAddressId)) {
                bindRadioBackground(view, position, true);
            } else {
                bindRadioBackground(view, position, false);
            }
            // 如果是编辑地址，设置为默认list背景
        } else if (mListType == TYPE_EDITLIST) {
            super.bindBackground(view, position);
        }

        if (view instanceof AddressListItem) {
            ((AddressListItem) view).bind(data);
            view.setTag(data);
        }
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return (mListType == TYPE_USERLIST) ? TYPE_USERLIST : TYPE_EDITLIST;
    }

    public void setCheckedAddressId(String addressId) {
        mCheckedAddressId = addressId;
        notifyDataSetChanged();
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
    /**
     * 给view设置radio背景，如果pressed状态为true，设置背景为选中状态
     *
     * @param view
     * @param position
     * @param isChecked 是否选中
     */
    protected void bindRadioBackground(View view, int position, boolean isChecked) {
        int resourceId;
        if (getCount() == 1) {
            resourceId = R.drawable.radiobutton_single_bg;
        } else {
            if (position == 0) {
                resourceId = isChecked ? R.drawable.radiobutton_up_bg_p
                        : R.drawable.radiobutton_up_bg_n;
            } else if (position == getCount() - 1) {
                resourceId = isChecked ? R.drawable.radiobutton_bottom_bg_p
                        : R.drawable.radiobutton_bottom_bg_n;
            } else {
                resourceId = isChecked ? R.drawable.radiobutton_middle_bg_p
                        : R.drawable.radiobutton_middle_bg_n;
            }
        }
        view.setBackgroundResource(resourceId);
    }
}
