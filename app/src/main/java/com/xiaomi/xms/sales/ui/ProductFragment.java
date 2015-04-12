
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.CampaignActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.adapter.PhoneTypeAdapter;
import com.xiaomi.xms.sales.adapter.ProductAdapter;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.ProductLoader;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.PhoneModelInfo;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.widget.BaseGridView;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.PageScrollListener;

import java.util.ArrayList;

public class ProductFragment extends BaseFragment implements
        LoaderCallbacks<ProductLoader.Result> {
    private final static int PRODUCT_LOADER = 1;
    private EmptyLoadingView mLoadingView;
    private BaseGridView mGridView;
    private BaseListView mPhoneFilterListView;
    private ProductAdapter mProductAdapter;
    private PhoneTypeAdapter mPhoneTypeAdapter;
    public String mCategoryId;
    private PopupWindow mPhoneFilterWindow;
    private TextView mTitle;
    private TextView mTitleFilterAll;
    private ImageView mDropDownIcon;
    private ImageView mChooseIcon;
    private LinearLayout mCustomView;
    private ArrayList<PhoneModelInfo> mPhoneModelInfo;
    private String mFilterChoice;
    private View mSortView, mSortDefault, mSortTime, mSortPrice, mSortComment, mSortPriceAscIcon,
            mSortPriceDescIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_fragment, container, false);
        mGridView = (BaseGridView) view.findViewById(R.id.grid_view);
        mProductAdapter = new ProductAdapter(getActivity(), true);// TODO:
                                                                  // 根据load的结果决定是否有筛选
        mGridView.setAdapter(mProductAdapter);
        mGridView.setOnItemClickListener(mGridItemClickListner);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mGridView.setOnScrollListener(mOnScrollListener);
        mPhoneTypeAdapter = new PhoneTypeAdapter(getActivity());
        mLoadingView.setEmptyText(R.string.filter_is_null);
        newPhoneFilterView();

        mSortView = view.findViewById(R.id.sort_view);
        mSortView.setVisibility(View.VISIBLE);// TODO: 根据load的结果决定是否有筛选
        mSortDefault = mSortView.findViewById(R.id.sort_default);
        mSortDefault.setSelected(true);
        mSortTime = mSortView.findViewById(R.id.sort_time);
        mSortPrice = mSortView.findViewById(R.id.sort_price);
        mSortComment = mSortView.findViewById(R.id.sort_comment);
        mSortDefault.setOnClickListener(mOnClickListener);
        mSortTime.setOnClickListener(mOnClickListener);
        mSortPrice.setOnClickListener(mOnClickListener);
        mSortComment.setOnClickListener(mOnClickListener);
        mSortPriceAscIcon = view.findViewById(R.id.sort_price_icon_asc);
        mSortPriceDescIcon = view.findViewById(R.id.sort_price_icon_desc);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mCategoryId = bundle.getString(Constants.Intent.EXTRA_CATEGORY_ID);
        }
        return view;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == mSortDefault) {
                if (v.isSelected()) {
                    return;
                }
                mSortPriceAscIcon.setVisibility(View.VISIBLE);
                mSortPriceDescIcon.setVisibility(View.GONE);
                mSortDefault.setSelected(true);
                mSortTime.setSelected(false);
                mSortPrice.setSelected(false);
                mSortComment.setSelected(false);
                ((ProductLoader) mLoader).setSortType(ProductLoader.SORT_DEFAULT);
                ((ProductLoader) mLoader).setPage(1);
                mLoader.forceLoad();
            } else if (v == mSortTime) {
                if (v.isSelected()) {
                    return;
                }
                mSortPriceAscIcon.setVisibility(View.VISIBLE);
                mSortPriceDescIcon.setVisibility(View.GONE);
                mSortDefault.setSelected(false);
                mSortTime.setSelected(true);
                mSortPrice.setSelected(false);
                mSortComment.setSelected(false);
                ((ProductLoader) mLoader).setSortType(ProductLoader.SORT_TIME);
                ((ProductLoader) mLoader).setPage(1);
                mLoader.forceLoad();
            } else if (v == mSortPrice) {
                String type = ProductLoader.SORT_PRICE_ASC;
                if (mSortPrice.isSelected()) {
                    if (mSortPriceAscIcon.getVisibility() == View.VISIBLE) {
                        mSortPriceAscIcon.setVisibility(View.GONE);
                        mSortPriceDescIcon.setVisibility(View.VISIBLE);
                        type = ProductLoader.SORT_PRICE_DESC;
                    } else {
                        mSortPriceAscIcon.setVisibility(View.VISIBLE);
                        mSortPriceDescIcon.setVisibility(View.GONE);
                    }
                }
                mSortDefault.setSelected(false);
                mSortTime.setSelected(false);
                mSortPrice.setSelected(true);
                mSortComment.setSelected(false);
                ((ProductLoader) mLoader).setSortType(type);
                ((ProductLoader) mLoader).setPage(1);
                mLoader.forceLoad();
            } else if (v == mSortComment) {
                if (v.isSelected()) {
                    return;
                }
                mSortPriceAscIcon.setVisibility(View.VISIBLE);
                mSortPriceDescIcon.setVisibility(View.GONE);
                mSortDefault.setSelected(false);
                mSortTime.setSelected(false);
                mSortPrice.setSelected(false);
                mSortComment.setSelected(true);
                ((ProductLoader) mLoader).setSortType(ProductLoader.SORT_COMMENT);
                ((ProductLoader) mLoader).setPage(1);
                mLoader.forceLoad();
            }
        }
    };

    private OnScrollListener mOnScrollListener = new MyPageScrollListener(new Runnable() {
        @Override
        public void run() {
            if (!mLoader.isLoading()) {
                if (((ProductLoader) mLoader).hasNextPage()) {
                    mLoader.forceLoad();
                }
            }
        }
    });

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Loader<ProductLoader.Result> onCreateLoader(int id, Bundle bundle) {
        if (id == PRODUCT_LOADER) {
            mLoader = new ProductLoader(getActivity(), mCategoryId);
            mLoader.setProgressNotifiable(mLoadingView);
            return (Loader<ProductLoader.Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<ProductLoader.Result> loader, ProductLoader.Result data) {
        mProductAdapter.updateData(data.mProductInfos);
        if (!TextUtils.isEmpty(data.mCateName)) {
            setCustomTitle(data.mCateName);
        }

        if (!data.mPhoneModelInfos.isEmpty()) {
            if (TextUtils.isEmpty(mFilterChoice)) {
                mFilterChoice = Tags.Phone.ALL_PHONE;
            }
            mPhoneModelInfo = data.mPhoneModelInfos;
            mPhoneTypeAdapter.updateData(data.mPhoneModelInfos);
            ((ProductLoader) mLoader).setNeedPhoneModelTask(false);
            mDropDownIcon.setVisibility(View.VISIBLE);
            mCustomView.setOnClickListener(mOnFilterClickListener);
        }

        if (data.mProductInfos != null && !data.mProductInfos.isEmpty()
                && ((ProductLoader) mLoader).getPage() == 1
                && mGridView.getFirstVisiblePosition() != 0) {
            mGridView.setAdapter(mProductAdapter);// 重新setAdapter仅仅是为了使GridView滑到顶部，因为调用setSelection()不起作用
        }
    }

    @Override
    public void onLoaderReset(Loader<ProductLoader.Result> loader) {
    }

    private OnItemClickListener mGridItemClickListner = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mProductAdapter.isHeaderItem(position)) {
                return;
            }

            ProductInfo mProductInfo = (ProductInfo) mProductAdapter.getItem(position);
            Intent intent = new Intent();
            // 浏览器打开
            if (Tags.Product.DISPLAY_BROWSER.equals(mProductInfo.getDisplayType())) {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mProductInfo.getUrl()));
            } else if (Tags.Product.DISPLAY_WEB.equals(mProductInfo.getDisplayType())) {
                // 应用中的Web界面打开
                intent.setClass(getActivity(), CampaignActivity.class);
                intent.putExtra(Constants.Intent.EXTRA_COMPAIGN_URL, mProductInfo.getUrl());
                CampaignActivity.startActivityStandard((BaseActivity) getActivity(),
                        mProductInfo.getUrl());
                return;
            } else if (!TextUtils.isEmpty(mProductInfo.getProductId())) {
                // 本地应用打开
                intent.setClass(getActivity(), ProductDetailsActivity.class);
                if (mProductInfo.isIsBatched()) {
                    // 如果是套餐
                    intent.putExtra(Constants.Intent.EXTRA_CONTAIN_ID, mProductInfo.getContainId());
                }
                intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, mProductInfo.getProductId());
                intent.putExtra(Constants.Intent.EXTRA_P_ID, mProductInfo.getPid());
                if (!TextUtils.isEmpty(mProductInfo.getUrl())) {
                    intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                    intent.putExtra(Constants.Intent.EXTRA_MIPHONE_NAME,
                            mProductInfo.getProductName());
                }
            }
            getActivity().startActivity(intent);
        }
    };

    private void newPhoneFilterView() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        mCustomView = (LinearLayout) layoutInflater.inflate(R.layout.filter_product, null);
        mTitle = (TextView) mCustomView.findViewById(R.id.bar_title);
        mTitleFilterAll = (TextView) mCustomView.findViewById(R.id.filter_all);
        mDropDownIcon = (ImageView) mCustomView.findViewById(R.id.choice_phone_type);
        mChooseIcon = (ImageView) mCustomView.findViewById(R.id.filter_phone_type_img);
        ((BaseActivity) getActivity()).setLeftView(mCustomView);
    }

    private OnClickListener mOnFilterClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.screening_container:
                    showPhoneFilterList(v);
                    break;
                default:
                    break;
            }
        }
    };

    private void showPhoneFilterList(View parent) {
        if (mPhoneFilterWindow == null) {
            LinearLayout view = (LinearLayout) LayoutInflater.from(getActivity())
                    .inflate(R.layout.phone_filter_list, null);
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.phone_type_item, null);
            TextView typeText = (TextView) v.findViewById(R.id.phone_type_text);
            ImageView typeImg = (ImageView) v.findViewById(R.id.phone_type_img);
            typeText.setText(R.string.allphone);
            typeImg.setImageDrawable(getResources().getDrawable(R.drawable.all_phone_icon));
            mPhoneFilterListView = (BaseListView) view.findViewById(android.R.id.list);
            mPhoneFilterListView.addHeaderView(v);
            mPhoneFilterListView.setAdapter(mPhoneTypeAdapter);
            mPhoneFilterListView.setOnItemClickListener(mListItemClickListner);
            mPhoneFilterWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);

            // 修复点击蒙版区域时筛选list无法收起的BUG
            LinearLayout popupLayout = (LinearLayout) view.findViewById(R.id.popup_layout);
            popupLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPhoneFilterWindow != null && mPhoneFilterWindow.isShowing()) {
                        mPhoneFilterWindow.dismiss();
                    }
                }
            });
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPhoneFilterWindow != null && mPhoneFilterWindow.isShowing()) {
                        mPhoneFilterWindow.dismiss();
                    }
                    ((ProductLoader) mLoader).setPhoneModel("");
                    ((ProductLoader) mLoader).setPage(1);
                    mLoader.setNeedDatabase(true);
                    mLoader.forceLoad();
                    mDropDownIcon.setVisibility(View.VISIBLE);
                    mChooseIcon.setVisibility(View.GONE);
                }
            });

        }
        mPhoneFilterWindow.setBackgroundDrawable(new BitmapDrawable());
        mPhoneFilterWindow.setTouchable(true);
        mPhoneFilterWindow.setFocusable(true);
        mPhoneFilterWindow.setOutsideTouchable(true);
        mPhoneFilterWindow.setAnimationStyle(0);
        mPhoneFilterWindow.showAsDropDown(parent);
        mPhoneFilterWindow.update();
        mPhoneFilterWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                rotateImage(mDropDownIcon, true);
            }
        });
        rotateImage(mDropDownIcon, false);
    }

    private void rotateImage(ImageView view, boolean isRestore) {
        Matrix matrix = new Matrix();
        if (!isRestore) {
            if (Device.DISPLAY_DENSITY == 480) {
                matrix.setRotate(180, 17, 12);
            } else if (Device.DISPLAY_DENSITY == 320 || Device.DISPLAY_DENSITY == 240) {
                matrix.setRotate(180, 11, 8);
            }
        }
        view.setImageMatrix(matrix);
        view.invalidate();
    }

    private OnItemClickListener mListItemClickListner = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // String phoneModelSymbol = mPhoneModelInfo.get(position -
            // 1).getPhoneSymbol();
            String phoneModelCode = String
                    .valueOf(mPhoneModelInfo.get(position - 1).getPhoneCode());
            Image image = mPhoneModelInfo.get(position - 1).getImage();
            ((ProductLoader) mLoader).setPhoneModel(phoneModelCode);
            ((ProductLoader) mLoader).setPage(1);
            mLoader.setNeedDatabase(true);
            mLoader.forceLoad();
            mPhoneFilterWindow.dismiss();
            mGridView.setSelection(0);
            mFilterChoice = phoneModelCode;
            mDropDownIcon.setVisibility(View.GONE);
            mChooseIcon.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().loadImage(mChooseIcon, image, null);

            setCustomTitle((String) mTitle.getText());
        }
    };

    public void setCustomTitle(String categoryName) {
        if (Tags.Phone.ALL_PHONE.equals(mFilterChoice)) {
            mTitleFilterAll.setVisibility(View.VISIBLE);
        } else {
            mTitleFilterAll.setVisibility(View.GONE);
        }
        mTitle.setText(categoryName);
    }

    private class MyPageScrollListener extends PageScrollListener {

        private int mLastYPostition = Integer.MAX_VALUE;
        private int mMaxMargin;
        private final int GRID_PADDING = (int) ShopApp.getContext().getResources()
                .getDimension(R.dimen.grid_item_padding);

        public MyPageScrollListener(Runnable callback) {
            super(callback);
            mMaxMargin = (int) -ShopApp.getContext().getResources()
                    .getDimension(R.dimen.product_grid_header_height);
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            if (mSortView == null || mSortView.getVisibility() != View.VISIBLE) {
                return;
            }
            View v = view.getChildAt(0);
            if (v != null) {
                int yPosition = -v.getTop();
                if (firstVisibleItem >= ProductAdapter.HEADER_ITEM_NUM) {
                    yPosition += -mMaxMargin
                            + (firstVisibleItem / ProductAdapter.HEADER_ITEM_NUM - 1)
                            * (v.getHeight() + GRID_PADDING);
                }
                if (mLastYPostition != Integer.MAX_VALUE) {
                    int dis = mLastYPostition - yPosition;
                    FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) mSortView
                            .getLayoutParams());
                    if (lp.topMargin + dis < mMaxMargin) {
                        lp.topMargin = mMaxMargin;
                    } else if (lp.topMargin + dis > 0) {
                        lp.topMargin = 0;
                    } else {
                        lp.topMargin = lp.topMargin + dis;
                    }
                    mSortView.setLayoutParams(lp);
                }
                mLastYPostition = yPosition;
            }
        }

    }

}
