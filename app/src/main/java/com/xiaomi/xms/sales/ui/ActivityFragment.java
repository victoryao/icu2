
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.gallery.GestureDetector;

public class ActivityFragment extends BaseWebFragment {
    private static final String TAG = "ActivityFragment";

    private static final String BOTTOM_STYLE_BUTTON = "button";
    private static final String BOTTOM_STYLE_BAR = "bar";

    private LinearLayout mBottom;
    private ImageView mBottomScrollBar;
    private LinearLayout mBottomScrollButton;
    private View mActivityContainer;
    private GestureDetector mGestrueDetector;

    // 活动相关变量
    private boolean mHasActivity;
    private String mPeviousActivityUrl;
    private Handler mHandler = new Handler();
    private String mCurrentBottomStyle;

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private float mPreDistanceY;
        private int mTotalScrollDistance;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            LogUtil.d("MyGestureListener", "The distance y is:" + distanceY);
            if (mPreDistanceY * distanceY > 0) {
                int distanceYInPixel = (int) (distanceY * 3.2);
                int totalScrollDistance = mTotalScrollDistance + distanceYInPixel;
                if (totalScrollDistance < Device.DISPLAY_HEIGHT && totalScrollDistance > 0) {
                    mTotalScrollDistance = totalScrollDistance;
                    mActivityContainer.scrollBy(0, distanceYInPixel);
                }
            }
            mPreDistanceY = distanceY;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            LogUtil.d("MyGestureListener", "The velocityY is:" + velocityY);
            if (velocityY < -5000) {
                showActivity(false);
            }
            return true;
        }

        @Override
        public boolean onUp(MotionEvent e) {
            // 如果滑动举例超过屏幕高度的1/4，那么收起活动页
            if (mTotalScrollDistance > (Device.DISPLAY_HEIGHT / 4)) {
                showActivity(false);
                final int dis = -mTotalScrollDistance;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mActivityContainer.scrollBy(0, dis);
                    }
                }, 1100);
            } else {
                // 否则回到起始的位置
                mActivityContainer.scrollBy(0, -mTotalScrollDistance);
            }
            mTotalScrollDistance = 0;
            mPreDistanceY = 0;
            return true;
        }
    }

    public String getUrl() {
        return mWebView.getUrl();
    }

    private WebViewLoadingListener mActivityWebViewLoadingListener = new WebViewLoadingListener() {
        @Override
        public void onLoadFinished() {
            if (mHasActivity) {
                showActivity(true);
                mHasActivity = false;
            }
        }
    };

    public void setBottomBarStyle(final String style) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCurrentBottomStyle = style;
                if (BOTTOM_STYLE_BAR.equals(style)) {
                    mBottomScrollBar.setVisibility(View.VISIBLE);
                    mBottomScrollButton.setVisibility(View.GONE);
                } else if (BOTTOM_STYLE_BUTTON.equals(style)) {
                    mBottomScrollBar.setVisibility(View.GONE);
                    mBottomScrollButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void initialize(View activityContainer) {
        mActivityContainer = activityContainer;
        mBottom = (LinearLayout) activityContainer.findViewById(R.id.activity_bottom);
        mGestrueDetector = new GestureDetector(getActivity(), new MyGestureListener(), null, true);
        mBottomScrollBar = (ImageView) activityContainer
                .findViewById(R.id.activity_bottom_scroll_bar);
        mBottomScrollButton = (LinearLayout) activityContainer
                .findViewById(R.id.activity_bottom_scroll_button);
        setBottomBarStyle(BOTTOM_STYLE_BUTTON);
        mBottom.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestrueDetector.onTouchEvent(event);
            }
        });
        mBottom.setOnClickListener(new OnClickListener() {
            private static final int JUMPED_HEIGHT = 60;
            @Override
            public void onClick(View v) {
                if (BOTTOM_STYLE_BAR.equals(mCurrentBottomStyle)) {
                    mActivityContainer.scrollBy(0, JUMPED_HEIGHT);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActivityContainer.scrollBy(0, -JUMPED_HEIGHT);
                        }
                    }, 100);
                } else if (BOTTOM_STYLE_BUTTON.equals(mCurrentBottomStyle)) {
                    showActivity(false);
                }
            }
        });
        setWebViewLoadingListener(mActivityWebViewLoadingListener);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_fragment;
    }

    @Override
    public void onLogout() {
        LogUtil.d(TAG, "User logout");
    }

    @Override
    public void onLogin(String userId, String authToken, String security) {
        LogUtil.d(TAG, "User login");
    }

    public void showActivity(boolean show) {
        if (getActivity() == null) {
            return;
        }
        if (show) {
            if (!isActivityVisible()) {
                // mActivityContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.appear_from_top));
                mActivityContainer.setVisibility(View.VISIBLE);
            }
        } else {
            if (isActivityVisible()) {
                // mActivityContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.disappear_from_bottom));
                mActivityContainer.setVisibility(View.GONE);
            }
            clearHistory();
        }
    }

    /**
     * 活动页是否可见
     */
    public boolean isActivityVisible() {
        return mActivityContainer.getVisibility() == View.VISIBLE;
    }

    public void onCheckActivityFinished(Intent callbackIntent) {
        String url = callbackIntent.getStringExtra(Constants.Intent.EXTRA_ACTIVITY_URL);
        String version = callbackIntent.getStringExtra(Constants.Intent.EXTRA_ACTIVITY_VERSION);
        String type = callbackIntent.getStringExtra(Constants.Intent.EXTRA_ACTIVITY_TYPE);
        boolean hasActivity = !TextUtils.isEmpty(url) && !TextUtils.isEmpty(version);
        if (hasActivity) {
            String prevVersion = Utils.Preference.getStringPref(getActivity(), Constants.Prefence.PREF_ACTIVITY_VERSION,
                    null);
            Utils.Preference.setStringPref(getActivity(), Constants.Prefence.PREF_ACTIVITY_VERSION, version);
            // 如果是同一个活动并且类型是预约，那么只弹出一次
            if (TextUtils.equals(prevVersion, version)
                    && TextUtils.equals(type, Tags.Activity.TYPE_RESERVE)) {
                return;
            }

            mHasActivity = true;
            // 如果之前的活动和现在活动页不一致，那么重新加载活动页
            if (!TextUtils.equals(url, mPeviousActivityUrl)) {
                loadUrl(url);
                Utils.Preference.setStringPref(getActivity(), Constants.Prefence.PREF_ACTIVITY_URL, url);
            } else {
                // 如果与预先加载的活动页与最新活动一致，那么直接显示
                LogUtil.d(TAG, "The same activity url: " + url);
                showActivity(true);
            }
        } else {
            Utils.Preference.removePref(getActivity(), Constants.Prefence.PREF_ACTIVITY_URL);
            mHasActivity = false;
        }
    }

    public void onCheckActivity() {
        // 如果当前活动页不可见并且记录着上次缓存的活动地址，那么不管这个活动是否还在进行，都
        // 先尝试加载。
        mPeviousActivityUrl = Utils.Preference.getStringPref(getActivity(),
                Constants.Prefence.PREF_ACTIVITY_URL, "");
        if (!TextUtils.isEmpty(mPeviousActivityUrl)) {
            loadUrl(mPeviousActivityUrl);
        }
    }
}
