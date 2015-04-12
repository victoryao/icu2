
package com.xiaomi.xms.sales.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static Toast lastToast = null;

    public static void clear() {
        if (lastToast != null) {
            lastToast.cancel();
        }
    }

    public static void show(Context context, int resId) {
        show(context, context.getString(resId), Toast.LENGTH_SHORT);
    }

    public static void show(Context context, CharSequence text) {
        show(context, text, Toast.LENGTH_LONG);
    }

    public static void show(Context context, int resId, int duration) {
        show(context, context.getString(resId), duration);
    }

    public static void show(Context context, CharSequence text, int duration) {
        if (lastToast != null) {
            lastToast.cancel();
        }
        Toast aToast = Toast.makeText(context, text, duration);
        lastToast = aToast;
        aToast.show();
    }
}
