
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.adapter.BasePageAdapter;
import com.xiaomi.xms.sales.adapter.ProductDetailPageAdapter;
import com.xiaomi.xms.sales.adapter.ProductDetailPageAdapter.OnPageItemClickListener;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.ProductDetailsLoader;
import com.xiaomi.xms.sales.loader.ProductDetailsLoader.Result;
import com.xiaomi.xms.sales.model.ProductDetailsInfo;
import com.xiaomi.xms.sales.model.ProductDetailsInfoItem;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ImageUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.gallery.ZoomImageView;

public class ProductDetailFullScreenFragment extends BaseFragment implements
        LoaderCallbacks<ProductDetailsLoader.Result>, OnPageItemClickListener, OnPageChangeListener {
    private static final String TAG = "ProductDetailFullScreenFragment";
    private static final int PRODUCT_DETAIL_LOADER = 0;
    private ViewPager mViewPager;
    private BasePageAdapter<ProductDetailsInfoItem> mProductPageAdapter;

    private View mActionBarBottom;
    private View mActionBarTop;
    private TextView mActionBarTile;
    private View mActionBarHome;
    private Button mProductDetailBtn;
    private Button mSaveImageBtn;
    private ProductDetailsInfo mProductDetailInfos;
    private String mProductId;
    private int mInitItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mProductId = getArguments().getString(Constants.Intent.EXTRA_PRODUCT_ID);
        mInitItem = getArguments().getInt(Constants.Intent.EXTRA_FULL_SCREEN_START_INDEX, -1);
        View view = inflater.inflate(R.layout.product_detail_full_screen_fragment, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.product_detail_pager);
        mProductPageAdapter = new ProductDetailPageAdapter(getActivity());
        ((ProductDetailPageAdapter) mProductPageAdapter).setOnPageItemClickListener(this);
        mViewPager.setAdapter(mProductPageAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOnTouchListener(mViewPagerOnTouchListener);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(
                R.dimen.view_pager_page_margin));
        initFloatingActionBar(view);
        return view;
    }

    private OnTouchListener mViewPagerOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // 如果当前没有数据，那么无法滑动
            if (mProductPageAdapter.getCount() == 0) {
                return true;
            }

            ZoomImageView currentImageView = (ZoomImageView) mViewPager.findViewById(
                    mViewPager.getCurrentItem()).findViewById(R.id.product_details_photo_frame);

            // 如果当前图片被放大了，那么无法滑动
            if (currentImageView.isZoomedOut()) {
                currentImageView.dispatchTouchEvent(event);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(PRODUCT_DETAIL_LOADER, null, this);
    }

    private void initFloatingActionBar(View view) {
        mActionBarTop = view.findViewById(R.id.action_bar_top);
        mActionBarBottom = view.findViewById(R.id.action_bar_bottom);
        mActionBarTile = (TextView)view.findViewById(R.id.action_bar_title);
        mActionBarHome = view.findViewById(R.id.action_bar_home);
        mActionBarHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        mSaveImageBtn = (Button)view.findViewById(R.id.save_image);
        mSaveImageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProductPageAdapter.getCount() > 0) {
                    int currentPos = mViewPager.getCurrentItem();
                    ProductDetailsInfoItem item = mProductPageAdapter.getData(currentPos);
                    Bitmap image = ImageLoader.getInstance().syncLoadLocalImage(item.getImage(), false);
                    if (image != null) {
                        String fileName = Constants.ExternalStorage.ROOT_SAVED
                                + mProductDetailInfos.getProductName() + "_"
                                + System.currentTimeMillis() + ".jpg";
                        if (ImageUtil.saveToFile(image, fileName)) {
                            MediaScannerConnection.scanFile(getActivity(), new String[] {
                                fileName }, null, null);
                            ToastUtil.show(
                                    getActivity(),
                                    getString(R.string.save_pic_success, fileName));
                        } else {
                            ToastUtil.show(getActivity(), R.string.save_pic_fail);
                        }
                    } else {
                        ToastUtil.show(getActivity(), R.string.save_pic_unavailiable);
                    }
                }
            }
        });

        mProductDetailBtn = (Button)view.findViewById(R.id.back_to_detail);
        if (isFromProductDetail()) {
            mProductDetailBtn.setText(R.string.back_to_detail);
        }
        mProductDetailBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFromProductDetail()) {
                    Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, mProductId);
                    getActivity().startActivity(intent);
                } else {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == PRODUCT_DETAIL_LOADER) {
            mLoader = new ProductDetailsLoader(getActivity());
            ((ProductDetailsLoader)mLoader).setProductId(mProductId);
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        mProductDetailInfos = data.mProductDetailsInfos;
        if (mProductDetailInfos != null) {
            mProductPageAdapter.updateData(mProductDetailInfos.getItems());
        } else {
            mProductPageAdapter.updateData(null);
        }

        int currentIndex = isFromProductDetail() ? mInitItem : 0;
        mViewPager.setCurrentItem(currentIndex);
        updateTitle(currentIndex + 1, mProductPageAdapter.getCount());
    }

    private void updateTitle(int index, int count) {
        mActionBarTile.setText(getResources().getString(R.string.pic_view_format, index,
                count));
    }

    private boolean isFromProductDetail() {
        return mInitItem != -1;
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

    @Override
    public void onClick() {
        if (mActionBarBottom.getVisibility() == View.VISIBLE) {
            mActionBarBottom.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.disappear_from_top));
            mActionBarBottom.setVisibility(View.GONE);
            mActionBarTop.setVisibility(View.GONE);
        } else {
            mActionBarBottom.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.appear_from_bottom));
            mActionBarBottom.setVisibility(View.VISIBLE);
            mActionBarTop.setVisibility(View.VISIBLE);
            updateTitle(mViewPager.getCurrentItem() + 1, mProductPageAdapter.getCount());
        }
    }

    @Override
    public void onPageScrollStateChanged(int position) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        updateTitle(mViewPager.getCurrentItem() + 1, mProductPageAdapter.getCount());
    }
}
