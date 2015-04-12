
package com.xiaomi.xms.sales.widget;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.ui.ComboFragment;
import com.xiaomi.xms.sales.ui.MiHomeProductDetailFragment;
import com.xiaomi.xms.sales.ui.ProductDetailsFragment;
import com.xiaomi.xms.sales.util.ToastUtil;

public class AddShoppingCartAnimation {

    private ViewGroup mAnimMaskLayout;
    private Activity mActivity;
    private View mStartView;
    private View mEndView;
    private boolean isMiHomeBuy;
    private boolean isCombo;
    private ViewGroup rootView;
    private Fragment mFragment;

    public AddShoppingCartAnimation(Activity activity, View startView, View endView,
            Fragment fragment) {
        mActivity = activity;
        mStartView = startView;
        mEndView = endView;
        mFragment = fragment;
        mAnimMaskLayout = createAnimLayout();
    }

    public AddShoppingCartAnimation(Activity activity, View startView, View endView,
            Fragment fragment, boolean isMiHomeBuy, boolean isCombo) {
        this(activity, startView, endView, fragment);
        this.isMiHomeBuy = isMiHomeBuy;
        this.isCombo = isCombo;
    }

    /**
     * @Description: 创建动画层
     */
    private ViewGroup createAnimLayout() {
        rootView = (ViewGroup) mActivity.getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(mActivity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    /**
     * @Description: 添加视图到动画层
     */
    private View addViewToAnimLayout(final ViewGroup vg, final View view,
            int[] location) {

        int x = location[0];
        int y = location[1];
        vg.addView(view);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }

    public void setAnim() {
        final ImageView imageView = new ImageView(mActivity);
        imageView.setImageResource(R.drawable.addcart_ani_pic);
        Animation zoomScaleAnimation = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        zoomScaleAnimation.setDuration(300);
        zoomScaleAnimation.setFillAfter(true);

        Animation startNarrowScaleAnimation = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        startNarrowScaleAnimation.setDuration(100);
        startNarrowScaleAnimation.setStartOffset(300);
        startNarrowScaleAnimation.setFillAfter(true);

        Animation endNarrowScaleAnimation = new ScaleAnimation(1.0f, 0f, 1.0f, 0f,
                Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.1f);
        endNarrowScaleAnimation.setDuration(500);
        endNarrowScaleAnimation.setStartOffset(650);
        endNarrowScaleAnimation.setFillAfter(true);

        Animation resilienceScaleAnimation = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        resilienceScaleAnimation.setDuration(100);
        resilienceScaleAnimation.setStartOffset(400);
        resilienceScaleAnimation.setInterpolator(new DecelerateInterpolator());
        resilienceScaleAnimation.setFillAfter(true);

        Animation resilienceScaleAnimation1 = new ScaleAnimation(1.0f, 0.67f, 1.0f, 0.67f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        resilienceScaleAnimation1.setDuration(100);
        resilienceScaleAnimation1.setInterpolator(new AccelerateInterpolator());
        resilienceScaleAnimation1.setStartOffset(500);
        resilienceScaleAnimation1.setFillAfter(true);

        int[] start_location = new int[2];
        mStartView.getLocationInWindow(start_location);
        // 将组件添加到我们的动画层上
        View view = addViewToAnimLayout(mAnimMaskLayout, imageView, start_location);
        int[] end_location = new int[2];
        mEndView.getLocationInWindow(end_location);
        // 计算位移
        int endX = end_location[0] - start_location[0];
        int endY = end_location[1] - start_location[1];

        Animation translateAnimation = new TranslateAnimation(0, endX, 0, endY);// 移动
        translateAnimation.setDuration(500);
        translateAnimation.setStartOffset(650);

        AnimationSet mAnimationSet = new AnimationSet(false);
        // 这块要注意，必须设为false,不然组件动画结束后，不会归位。
        mAnimationSet.setFillAfter(false);
        mAnimationSet.addAnimation(zoomScaleAnimation);
        mAnimationSet.addAnimation(startNarrowScaleAnimation);
        mAnimationSet.addAnimation(resilienceScaleAnimation);
        mAnimationSet.addAnimation(resilienceScaleAnimation1);
        mAnimationSet.addAnimation(endNarrowScaleAnimation);
        mAnimationSet.addAnimation(translateAnimation);
        view.startAnimation(mAnimationSet);

        mAnimationSet.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (isMiHomeBuy) {
                    ((MiHomeProductDetailFragment) mFragment).onAddShoppingCartFinish();
                    ((BaseActivity) mActivity).updateShoppingCount();
                    ((MiHomeProductDetailFragment) mFragment).showGotoCartWindow();
                } else if (isCombo) {
                    ((ComboFragment) mFragment).onAddShoppingCartFinish();
                    ((BaseActivity) mActivity).updateShoppingCount();
                    ((ComboFragment) mFragment).showGotoCartWindow();
                } else {
                    ((ProductDetailsFragment) mFragment).onAddShoppingCartFinish();
                    ((BaseActivity) mActivity).updateShoppingCount();
                    ((ProductDetailsFragment) mFragment).showGotoCartWindow();
                }

                ToastUtil.show(mActivity, mActivity.getString(R.string.already_add_shopping_cart));
                mAnimMaskLayout.removeViewInLayout(imageView);
            }
        });
    }

}
