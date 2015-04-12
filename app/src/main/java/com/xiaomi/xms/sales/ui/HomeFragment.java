
package com.xiaomi.xms.sales.ui;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.activity.MainActivity;
import com.xiaomi.xms.sales.adapter.HomeAdapter;
import com.xiaomi.xms.sales.loader.HomeLoader;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.HomeLoader.Result;
import com.xiaomi.xms.sales.model.HomeInfo;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.PageScrollListener;

import java.util.ArrayList;

public class HomeFragment extends BaseFragment implements LoaderCallbacks<HomeLoader.Result> {

    private static final int PHOTO_BOX_HEIGHT;
    private static final int PHOTO_BIG_BOX_HEIGHT;

    static {
        PHOTO_BOX_HEIGHT = (int) ShopApp.getContext().getResources()
                .getDimension(R.dimen.home_photo_box_height);
        PHOTO_BIG_BOX_HEIGHT = (int) ShopApp.getContext().getResources()
                .getDimension(R.dimen.home_big_photo_box_height);
    }

    private static final String TAG = "HomeFragment";
    private static final int HOME_LOADER = 0;
    private BaseListView mListView;
    private HomeAdapter mHomeAdapter;
    private HomeInfo mFirstHomeInfo;
    private EmptyLoadingView mLoadingView;
    private View mFooterView;
    private View mHeaderBigImageView;
    private ImageView mBigImageView;
    private View mUpIconView;
    private FrameLayout mBigImageFrameLayout;
    private ImageView mActivityTypePhoto;
    private TextView mFullPrice;
    private TextView mProductPrice;
    private TextView mProductDetail;
    private TextView mProductName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        View headerView = inflater.inflate(R.layout.main_tab_header_view, null);
        mHeaderBigImageView = inflater.inflate(R.layout.home_big_item, null);
        inflaterBigImageView(mHeaderBigImageView);
        mListView.addHeaderView(headerView, null, false);
        mListView.addHeaderView(mHeaderBigImageView);
        mHomeAdapter = new HomeAdapter(getActivity());
        mListView.setAdapter(mHomeAdapter);
        mListView.setOnScrollListener(new PageScrollListener(null));
        mListView.setOnItemClickListener(mItemClickListner);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mFooterView = inflater.inflate(R.layout.home_view_footer, null);
        mListView.addFooterView(mFooterView);
        mHeaderBigImageView.setVisibility(View.GONE);
        mFooterView.setVisibility(View.GONE);
        mListView.setOnScrollListener(new OnScrollListener() {
            int firstVisible = 0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    if (firstVisible >= 2) {
                        switcherImage();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                firstVisible = firstVisibleItem;
            }
        });

        mHeaderBigImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeBaseListItem.viewProductDetail(getActivity(), mFirstHomeInfo, false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switcherImage();
                    }
                }, 1000);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(HOME_LOADER, null, this);
        mFooterView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.launchMain(getActivity(), MainActivity.FRAGMENT_TAG_CATEGORY);
            }
        });
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == HOME_LOADER) {
            mLoader = new HomeLoader(getActivity());
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }
        return null;
    }

    private void inflaterBigImageView(View view) {
        mBigImageFrameLayout = (FrameLayout) view.findViewById(R.id.item_photo_layout);
        mBigImageView = (ImageView) view.findViewById(R.id.home_big_photo);
        mActivityTypePhoto = (ImageView) view.findViewById(R.id.activity_type_photo);
        mProductName = (TextView) view.findViewById(R.id.product_name);
        mProductDetail = (TextView) view.findViewById(R.id.product_detial);
        mProductPrice = (TextView) view.findViewById(R.id.product_price);
        mFullPrice = (TextView) view.findViewById(R.id.full_price);
        mFullPrice.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
        mUpIconView = view.findViewById(R.id.up_icon_layout);

        mUpIconView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListView.setOnScrollListener(null);
                mListView.smoothScrollBy(PHOTO_BIG_BOX_HEIGHT -PHOTO_BOX_HEIGHT, 300);
                mUpIconView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                        R.anim.disappear));
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mUpIconView.setVisibility(View.GONE);
                        mBigImageFrameLayout.setMinimumHeight(PHOTO_BOX_HEIGHT);
                        ImageLoader.getInstance().loadImage(mBigImageView, mFirstHomeInfo.getHomePhoto(),
                                R.drawable.default_pic_small_inverse);
                        mListView.smoothScrollBy(PHOTO_BOX_HEIGHT-PHOTO_BIG_BOX_HEIGHT, 0);
                    }
                }, 300);
            }
        });
    }

    public int getCurrentPosition() {
        return mListView == null ? 0 : mListView.getFirstVisiblePosition();
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        ArrayList<HomeInfo> homeInfo = new ArrayList<HomeInfo>();

        if (data.mHomeInfos != null && data.mHomeInfos.size() > 0) {
            mHeaderBigImageView.setVisibility(View.VISIBLE);
            mFooterView.setVisibility(View.VISIBLE);
            mFirstHomeInfo = data.mHomeInfos.get(0);
            homeInfo.addAll(data.mHomeInfos);
        }
        if (data.mHomeInfos != null && data.mHomeInfos.size() > 1) {
            homeInfo.remove(0);
        } else {
            homeInfo = data.mHomeInfos;
        }
        setFirstViewData();
        mHomeAdapter.updateData(homeInfo);
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

    private OnItemClickListener mItemClickListner = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HomeBaseListItem.viewProductDetail(getActivity(), (HomeInfo) view.getTag(), false);
        }
    };

    private void setFirstViewData() {
        if (mFirstHomeInfo != null) {
            mProductName.setText(mFirstHomeInfo.getProductName());
            mProductDetail.setText(mFirstHomeInfo.getProductDetail());
            mProductPrice.setText(getString(R.string.home_product_price_format,
                    mFirstHomeInfo.getProductPrice()));
            mFullPrice.setText(getString(R.string.home_product_price_format,
                    mFirstHomeInfo.getFullPrice()));

            if (null == mFirstHomeInfo.getActivityIcon()) {
                mActivityTypePhoto.setImageResource(0);
                mActivityTypePhoto.setVisibility(View.GONE);
            } else {
                mActivityTypePhoto.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().loadImage(mActivityTypePhoto,
                        mFirstHomeInfo.getActivityIcon(), 0);
            }

            if (TextUtils.isEmpty(mFirstHomeInfo.getProductPrice())) {
                mProductPrice.setVisibility(View.GONE);
            } else {
                mProductPrice.setVisibility(View.VISIBLE);
            }

            if (TextUtils.isEmpty(mFirstHomeInfo.getFullPrice())
                    || TextUtils.equals(mFirstHomeInfo.getFullPrice(),
                            mFirstHomeInfo.getProductPrice())) {
                mFullPrice.setVisibility(View.GONE);
            } else {
                mFullPrice.setVisibility(View.VISIBLE);
            }

            if (mFirstHomeInfo.getHomeBigPhoto() != null) {
                ImageLoader.getInstance().loadImage(mBigImageView, mFirstHomeInfo.getHomeBigPhoto(),
                        R.drawable.default_pic_small_inverse);
                mBigImageFrameLayout.setMinimumHeight(PHOTO_BIG_BOX_HEIGHT);
                ImageLoader.getInstance().syncLoadLocalImage(mFirstHomeInfo.getHomePhoto(), false);
                mUpIconView.setVisibility(View.VISIBLE);
            } else {
                mBigImageFrameLayout.setMinimumHeight(PHOTO_BOX_HEIGHT);
                ImageLoader.getInstance().loadImage(mBigImageView, mFirstHomeInfo.getHomePhoto(),
                        R.drawable.default_pic_small_inverse);
                mUpIconView.setVisibility(View.GONE);
            }
            mFirstHomeInfo.mHomeBigPhoto = null;
        }
    }

    public void switcherImage() {
        if (mUpIconView != null) {
            mUpIconView.setVisibility(View.GONE);
        }
        if (mBigImageFrameLayout != null) {
            mBigImageFrameLayout.setMinimumHeight(PHOTO_BOX_HEIGHT);
        }

        if (mFirstHomeInfo != null) {
            ImageLoader.getInstance().loadImage(mBigImageView, mFirstHomeInfo.getHomePhoto(),
                    R.drawable.default_pic_small_inverse);
        }
        mListView.setOnScrollListener(null);
    }

}
