
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.MainActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.SaleOutRegisterLoader;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.ui.ProductDetailsFragment.RecommendProductInfo;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

import java.util.ArrayList;

public class SaleOutRegisterResultFragment extends BaseFragment implements
        LoaderCallbacks<SaleOutRegisterLoader.Result> {
    private final static int REGISTER_LOADER = 1;
    private View mRegOkView;
    private LinearLayout mRecommendLayout;
    private EmptyLoadingView mLoadingView;
    private String mProductId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sale_out_register_result_fragment, container, false);
        mRegOkView = view.findViewById(R.id.reg_ok_view);
        mRegOkView.setVisibility(View.GONE);
        ((TextView) mRegOkView.findViewById(R.id.title_tv)).getPaint().setFakeBoldText(true);
        mRecommendLayout = (LinearLayout) view.findViewById(R.id.recommend_layout);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        View seeMoreBtn = view.findViewById(R.id.see_more);
        seeMoreBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.launchMain(getActivity(), MainActivity.FRAGMENT_TAG_CATEGORY);
            }
        });
        Bundle bundle = getArguments();
        if (bundle != null) {
            mProductId = bundle.getString(Constants.Intent.EXTRA_PRODUCT_ID);
        }
        return view;
    }

    private void initRecommendProductsView(ArrayList<ProductInfo> recommendProducts) {
        mRecommendLayout.setVisibility(View.VISIBLE);
        mRecommendLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        int size = recommendProducts.size();
        for (int i = 0; i < size; ++i) {
            ProductInfo one = recommendProducts.get(i);
            View recView = inflater.inflate(R.layout.recommend_item,
                    mRecommendLayout, false);
            RecommendProductInfo tag = new RecommendProductInfo(one, i);
            recView.setTag(tag);
            recView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecommendProductInfo tag = (RecommendProductInfo) v.getTag();
                    ProductInfo info = tag.info;
                    // 本地应用打开
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID,
                            info.getProductId());
                    if (!TextUtils.isEmpty(info.getUrl())) {
                        intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                        intent.putExtra(Constants.Intent.EXTRA_MIPHONE_NAME,
                                info.getProductName());
                    }
                    getActivity().startActivity(intent);
                }
            });
            ImageView iv = (ImageView) recView
                    .findViewById(R.id.recommend_item_image);
            TextView tv = (TextView) recView
                    .findViewById(R.id.recommend_item_price);
            ImageLoader.getInstance().loadImage(iv, one.getImage(),
                    R.drawable.default_pic_large);
            tv.setText(getString(R.string.rmb_identification,
                    one.getProductPrice()));
            mRecommendLayout.addView(recView);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(REGISTER_LOADER, null, this);
    }

    @Override
    public Loader<SaleOutRegisterLoader.Result> onCreateLoader(int id, Bundle bundle) {
        if (REGISTER_LOADER == id) {
            mLoader = new SaleOutRegisterLoader(getActivity(), mProductId);
            mLoader.setProgressNotifiable(mLoadingView);
            return (Loader<SaleOutRegisterLoader.Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<SaleOutRegisterLoader.Result> loader,
            SaleOutRegisterLoader.Result result) {
        if (result.isSuc) {
            mRegOkView.setVisibility(View.VISIBLE);
            if (!result.recommandProducts.isEmpty()) {
                initRecommendProductsView(result.recommandProducts);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<SaleOutRegisterLoader.Result> loader) {
    }
}
