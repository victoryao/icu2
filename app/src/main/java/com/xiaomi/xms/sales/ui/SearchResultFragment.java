
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.CampaignActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.adapter.ProductAdapter;
import com.xiaomi.xms.sales.loader.SearchResultLoader;
import com.xiaomi.xms.sales.loader.SearchResultLoader.Result;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.BaseGridView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.PageScrollListener;

public class SearchResultFragment extends BaseFragment implements
        LoaderCallbacks<SearchResultLoader.Result> {

    private final static int SEARCH_RESULT_LOADER = 1;
    private EmptyLoadingView mLoadingView;
    private BaseGridView mGridView;
    private ProductAdapter mSearchProductAdapter;
    public String mCategoryId;
    private String mKeyWord;
    private String mShowWord;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_fragment, container, false);
        mGridView = (BaseGridView) view.findViewById(R.id.grid_view);
        mSearchProductAdapter = new ProductAdapter(getActivity(), false);
        mGridView.setAdapter(mSearchProductAdapter);
        mGridView.setOnItemClickListener(mGridItemClickListner);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mGridView.setOnScrollListener(mOnScrollListener);
        mLoadingView.setEmptyText(R.string.search_result_is_null);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mKeyWord = bundle.getString(Constants.Intent.EXTRA_SEARCH_RESULT_KEYWORD);
            mCategoryId = bundle.getString(Constants.Intent.EXTRA_CATEGORY_ID);
        }
        mShowWord = "";
        if (mKeyWord != null) {
            if (mKeyWord.length() > 8) {
                mShowWord = mKeyWord.substring(0, 8) + "...";
            } else {
                mShowWord = mKeyWord;
            }
        }
        getActivity().setTitle(getString(R.string.search_title, mShowWord));
        return view;
    }

    private OnScrollListener mOnScrollListener = new PageScrollListener(new Runnable() {
        @Override
        public void run() {
            if (!mLoader.isLoading()) {
                if (((SearchResultLoader) mLoader).hasNextPage()) {
                    mLoader.forceLoad();
                }
            }
        }
    });

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SEARCH_RESULT_LOADER, null, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Loader<Result> onCreateLoader(int id, Bundle arg1) {
        if (id == SEARCH_RESULT_LOADER) {
            mLoader = new SearchResultLoader(getActivity(), mCategoryId, mKeyWord);
            mLoader.setProgressNotifiable(mLoadingView);
            return (Loader<SearchResultLoader.Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> arg0, Result data) {
        mSearchProductAdapter.updateData(data.mProductInfos);
        if (!TextUtils.isEmpty(data.mTotalCount)) {
            getActivity().setTitle(
                    getString(R.string.search_result_title, mShowWord, data.mTotalCount));
        } else {
            getActivity().setTitle(getString(R.string.search_result_title, mShowWord, "0"));
        }
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

    private OnItemClickListener mGridItemClickListner = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            ProductInfo mProductInfo = (ProductInfo) mSearchProductAdapter.getItem(position);
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
}
