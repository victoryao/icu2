
package com.xiaomi.xms.sales.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.BaseWebFragment;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.widget.gallery.GestureDetector;

public class CampaignActivity extends BaseActivity {
    public static final String BOTTOM_STYLE_BUTTON = "button";
    public static final String BOTTOM_STYLE_BAR = "bar";
    public static final String BOTTOM_STYLE_NONE = "none";

    private BaseWebFragment mCampainFragment;

    private View mActivityContainer;
    private LinearLayout mBottom;
    private ImageView mBottomScrollBar;
    private LinearLayout mBottomScrollButton;

    private GestureDetector mGestrueDetector;
    private boolean mFinishAnim;
    private String mCurrentBottomStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.campaign_activity);
        mActivityContainer = findViewById(R.id.container);
        mCampainFragment = (BaseWebFragment) getSupportFragmentManager().findFragmentById(
                R.id.campaign_fragment);
        mBottom = (LinearLayout) findViewById(R.id.activity_bottom);
        mBottomScrollBar = (ImageView) findViewById(R.id.activity_bottom_scroll_bar);
        mBottomScrollButton = (LinearLayout) findViewById(R.id.activity_bottom_scroll_button);
        mGestrueDetector = new GestureDetector(this, new MyGestureListener(), null, true);

        handleIntent(getIntent());

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
                    finish();
                }
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (intent.getBooleanExtra(Constants.Intent.EXTRA_CAMPAIGN_SHOW_TITLE, true)) {
            showTitleBar(null);
        } else {
            hideTitleBar();
        }

        String bottomStyle = intent.getStringExtra(Constants.Intent.EXTRA_CAMPAIGN_SHOW_BOTTOM);
        setBottomBarStyle(TextUtils.isEmpty(bottomStyle) ? BOTTOM_STYLE_NONE : bottomStyle);
        mFinishAnim = intent.getBooleanExtra(Constants.Intent.EXTRA_CAMPAIGN_FINISH_ANIM, false);

        String url = intent.getStringExtra(Constants.Intent.EXTRA_COMPAIGN_URL);
        if (!TextUtils.isEmpty(url)) {
            showWebView(url);
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CampaignActivity.super.finish();
                if (mFinishAnim) {
                    overridePendingTransition(R.anim.campaign_disappear_enter,
                            R.anim.campaign_disappear_exit);
                } else {
                    overridePendingTransition(R.anim.activity_close_enter,
                            R.anim.activity_close_exit);
                }
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void showWebView(String url) {
        mCampainFragment.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (!mCampainFragment.handleBackPressed()) {
            super.onBackPressed();
        }
    }

    public void showTitleBar(final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitleBarEnable(true);
                if (!TextUtils.isEmpty(title)) {
                    setTitle(title);
                }
            }
        });
    }

    public void hideTitleBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitleBarEnable(false);
            }
        });
    }

    public void setBottomBarStyle(final String style) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCurrentBottomStyle = style;
                if (BOTTOM_STYLE_BAR.equals(style)) {
                    mBottomScrollBar.setVisibility(View.VISIBLE);
                    mBottomScrollButton.setVisibility(View.GONE);
                } else if (BOTTOM_STYLE_BUTTON.equals(style)) {
                    mBottomScrollBar.setVisibility(View.GONE);
                    mBottomScrollButton.setVisibility(View.VISIBLE);
                } else if (BOTTOM_STYLE_NONE.equals(style)) {
                    mBottomScrollBar.setVisibility(View.GONE);
                    mBottomScrollButton.setVisibility(View.GONE);
                }
            }
        });
    }

    public static void startActivityWithAnimation(BaseActivity context, String url) {
        Intent intent = new Intent(context, CampaignActivity.class);
        intent.putExtra(Constants.Intent.EXTRA_COMPAIGN_URL, url);
        intent.putExtra(Constants.Intent.EXTRA_CAMPAIGN_SHOW_BOTTOM,
                CampaignActivity.BOTTOM_STYLE_BUTTON);
        intent.putExtra(Constants.Intent.EXTRA_CAMPAIGN_SHOW_TITLE, false);
        intent.putExtra(Constants.Intent.EXTRA_CAMPAIGN_FINISH_ANIM, true);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.campaign_appear_enter, R.anim.campaign_appear_exit);
    }

    public static void startActivityStandard(BaseActivity context, String url) {
        Intent intent = getStandardIntentToMe(context, url);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
    }

    public static Intent getStandardIntentToMe(Context context, String url) {
        Intent intent = new Intent(context, CampaignActivity.class);
        intent.putExtra(Constants.Intent.EXTRA_COMPAIGN_URL, url);
        return intent;
    }

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
                finish();
            }
            return true;
        }

        @Override
        public boolean onUp(MotionEvent e) {
            // 如果滑动举例超过屏幕高度的1/4，那么收起活动页
            if (mTotalScrollDistance > (Device.DISPLAY_HEIGHT / 4)) {
                finish();
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
}
