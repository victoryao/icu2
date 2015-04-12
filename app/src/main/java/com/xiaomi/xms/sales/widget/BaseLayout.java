
package com.xiaomi.xms.sales.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

public class BaseLayout extends RelativeLayout {

    public interface BackKeyListener {
        public boolean shouldHackBackKey();

        public void onBackKeyFired();
    }

    protected WeakReference<Activity> mActivityRef;
    protected WeakReference<BackKeyListener> mListener;

    public BaseLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BaseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseLayout(Context context) {
        super(context);
    }

    public void setActivity(Activity activity) {
        mActivityRef = new WeakReference<Activity>(activity);
    }

    public void setBackKeyListener(BackKeyListener listener) {
        mListener = new WeakReference<BackKeyListener>(listener);
    }

    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        Activity activity = mActivityRef == null ? null : mActivityRef.get();
        BackKeyListener listener = mListener == null ? null : mListener.get();
        if (listener != null && listener.shouldHackBackKey() && activity != null
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            KeyEvent.DispatcherState state = getKeyDispatcherState();
            if (state != null) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getRepeatCount() == 0) {
                    state.startTracking(event, this);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP
                        && !event.isCanceled() && state.isTracking(event)) {
                    hideInputMethod();
                    listener.onBackKeyFired();
                    return true;
                }
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

}
