
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.BasePageAdapter;
import com.xiaomi.xms.sales.adapter.HomePageAdapter;
import com.xiaomi.xms.sales.adapter.HomePageAdapter.OnPageItemClickListener;
import com.xiaomi.xms.sales.loader.HomeLoader;
import com.xiaomi.xms.sales.loader.HomeLoader.Result;
import com.xiaomi.xms.sales.model.HomeInfo;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils.Network;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;

public class HomeFullScreenFragment extends BaseFragment implements
        LoaderCallbacks<HomeLoader.Result>, OnPageChangeListener, OnPageItemClickListener {
    private static final String TAG = "HomeFullScreenFragment";
    private static final int HOME_LOADER = 0;
    private ViewPager mViewPager;
    private BasePageAdapter<HomeInfo> mHomePageAdapter;

    private View mFloatingActionBar;
    private TextView mProductName;
    private TextView mProductPrice;
    private TextView mFullPrice;
    private View mFullScreenViewBtn;
    private int mStartIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_full_screen_fragment, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.home_pager);
        initFloatingActionBar(view);
        mHomePageAdapter = new HomePageAdapter(getActivity());
        ((HomePageAdapter) mHomePageAdapter).setOnPageItemClickListener(this);
        mViewPager.setAdapter(mHomePageAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(
                R.dimen.view_pager_page_margin));
        if (getArguments() != null) {
            mStartIndex = getArguments().getInt(
                    Constants.Intent.EXTRA_FULL_SCREEN_START_INDEX, 0);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!shouldUserConfirm()) {
            getLoaderManager().initLoader(HOME_LOADER, null, this);
        }
    }

    private void initFloatingActionBar(View view) {
        mFloatingActionBar = view.findViewById(R.id.floating_action_bar);
        mProductName = (TextView) view.findViewById(R.id.product_name);
        mProductPrice = (TextView) view.findViewById(R.id.product_price);
        mFullPrice = (TextView) view.findViewById(R.id.full_price);
        mFullPrice.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
        mFullScreenViewBtn = view.findViewById(R.id.view_detail);
        mFullScreenViewBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeBaseListItem.viewProductDetail(getActivity(), (HomeInfo) v.getTag(), true);
            }
        });
    }

    private void updateFloatingActionBar(HomeInfo data) {
        mFullScreenViewBtn.setTag(data);
        mProductName.setText(data.getProductName());

        if (TextUtils.isEmpty(data.getFullPrice())
                || TextUtils.equals(data.getFullPrice(), data.getProductPrice())) {
            mFullPrice.setVisibility(View.GONE);
        } else {
            mFullPrice.setVisibility(View.VISIBLE);
            mFullPrice.setText(getActivity().getString(R.string.home_product_origin_price_format,
                    data.getFullPrice()));
        }

        if (TextUtils.isEmpty(data.getProductPrice())) {
            mProductPrice.setVisibility(View.GONE);
        } else {
            mProductPrice.setVisibility(View.VISIBLE);
            if (mFullPrice.getVisibility() == View.VISIBLE) {
                mProductPrice.setText(getActivity().getString(
                        R.string.home_product_spec_price_format,
                        data.getProductPrice()));
            } else {
                mProductPrice.setText(getActivity().getString(R.string.home_product_price_format,
                        data.getProductPrice()));
            }
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == HOME_LOADER) {
            mLoader = new HomeLoader(getActivity());
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        mHomePageAdapter.updateData(data.mHomeInfos);
        if (mStartIndex < mHomePageAdapter.getCount()) {
            mViewPager.setCurrentItem(mStartIndex);
            HomeInfo homeData = mHomePageAdapter.getData(mViewPager.getCurrentItem());
            mProductName.setText(homeData.getProductName());
            updateFloatingActionBar(homeData);
        }
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

    @Override
    public void onPageScrollStateChanged(int position) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        HomeInfo data = mHomePageAdapter.getData(mViewPager.getCurrentItem());
        updateFloatingActionBar(data);
    }

    @Override
    public void onClick() {
        if (mFloatingActionBar.getVisibility() == View.VISIBLE) {
            mFloatingActionBar.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.disappear_from_top));
            mFloatingActionBar.setVisibility(View.GONE);
        } else {
            mFloatingActionBar.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.appear_from_bottom));
            mFloatingActionBar.setVisibility(View.VISIBLE);
            HomeInfo data = mHomePageAdapter.getData(mViewPager.getCurrentItem());
            updateFloatingActionBar(data);
        }
    }

    private boolean shouldUserConfirm() {
        if (Network.isMobileConnected(getActivity())) {
            BaseAlertDialog dialog = new BaseAlertDialog(getActivity());
            dialog.setTitle(R.string.home_full_screen_appear_title);
            dialog.setMessage(R.string.home_full_screen_appear_summary);
            dialog.setPositiveButton(R.string.home_full_screen_use, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getLoaderManager().initLoader(HOME_LOADER, null, HomeFullScreenFragment.this);
                }
            });

            dialog.setNegativeButton(R.string.home_full_screen_discard,
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getActivity().setResult(Activity.RESULT_CANCELED);
                                getActivity().finish();
                            }
                        });
            dialog.setCancelable(false);
            dialog.show();
            return true;
        }
        return false;
    }
}
