package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.ProductDetailsFragment;

public class ChooseStyleWindow extends PopupWindow {

    private static final int BACKGROUND_COLOR = 0x99000000;
    private Context mContext;
    private View mContentView;
    private FrameLayout mRootPanel;
    public View mCoverView;
    private ProductDetailsFragment mFragment;

    public ChooseStyleWindow(Context context, View contentView, int height, ProductDetailsFragment fragment) {
        super(contentView, LayoutParams.MATCH_PARENT, height);
        mContext = context;
        mContentView = contentView;
        mFragment = fragment;
        mRootPanel = new RootPanel(mContext);
        if (mCoverView == null) {
            mCoverView = new LinearLayout(mRootPanel.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mCoverView.setBackgroundColor(BACKGROUND_COLOR);
            mRootPanel.addView(mCoverView, lp);
        }

        FrameLayout.LayoutParams r = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        r.gravity = Gravity.BOTTOM;
        mRootPanel.addView(contentView, r);
        setContentView(mRootPanel);
        setBackgroundDrawable(context.getResources().getDrawable(android.R.color.transparent));

        mCoverView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowing()) {
                    mFragment.hideStyleView();
                }
            }
        });
    }

    @Override
    public void dismiss() {
        Animation menuMoveDown = AnimationUtils.loadAnimation(mContext, R.anim.menu_exit);
        Animation bgDisappear = AnimationUtils.loadAnimation(mContext, R.anim.disappear);
        mCoverView.startAnimation(bgDisappear);
        mContentView.startAnimation(menuMoveDown);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    ChooseStyleWindow.super.dismiss();
                } catch (Exception e) {
                }
            }
        }, 300);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);
        mContentView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.menu_enter));
        mCoverView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.appear));
    }

    @Override
    public void showAtLocation(View anchor, int gravity, int x, int y) {
        super.showAtLocation(anchor, gravity, x, y);
        mContentView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.menu_enter));
        mCoverView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.appear));
    }

    private class RootPanel extends FrameLayout {
        public RootPanel(Context context) {
            super(context);
            setClickable(true);
        }

        @Override
        public void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
        }

        // Override performClick rather than set clickListener to avoid sound effect
        @Override
        public boolean performClick() {
//            dismiss();
            return true;
        }
    }

}
