
package com.xiaomi.xms.sales;

import android.app.Activity;
import android.content.ComponentName;
import android.text.TextUtils;

public class ShopIntentServiceAction {

    private String mAction;
    private ShopIntentService.Listener mListener;

    public ShopIntentServiceAction(String action, ShopIntentService.Listener listener) {
        mAction = action;
        mListener = listener;
    }

    public String getAction() {
        return mAction;
    }

    public ShopIntentService.Listener getListener() {
        return mListener;
    }

    @Override
    public boolean equals(Object actionObj) {
        if (!(actionObj instanceof ShopIntentServiceAction)) {
            return false;
        }
        ShopIntentServiceAction action = (ShopIntentServiceAction) actionObj;
        if (TextUtils.equals(mAction, action.getAction())) {
            ShopIntentService.Listener otherListener = action.getListener();
            if (!(otherListener instanceof Activity)) {
                return false;
            }
            ComponentName my = ((Activity) mListener).getComponentName();
            ComponentName other = ((Activity) otherListener).getComponentName();
            if (my.equals(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mAction.hashCode() * 31 + mListener.hashCode();
    }
}
