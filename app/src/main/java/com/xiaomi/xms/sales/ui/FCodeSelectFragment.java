
package com.xiaomi.xms.sales.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.adapter.FCodeSelectAdapter;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.RequestLoader;
import com.xiaomi.xms.sales.loader.RequestLoader.Result;
import com.xiaomi.xms.sales.model.ActionResult;
import com.xiaomi.xms.sales.model.FcodeSelectProduct;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

import java.util.ArrayList;

public class FCodeSelectFragment extends BaseFragment {

    // LoaderManager用来区别不同的Loader
    private static final int REQUEST_LOADER = 0;
    // RequestLoader用来区别不同的Request
    private static final int REQUEST_CODE_ADD_SHOPPING = 0;

    private BaseListView mListView;
    private EmptyLoadingView mLoadingView;
    private FCodeSelectAdapter mAdapter;
    private RequestLoader mLoader;
    private ProgressDialog mProgressDialog;
    private String mCheckedProductId;
    private Button mSubmitButton;
    private View mLisContainer;
    private View mSingleContainer;
    private ImageView mImage;
    private TextView mProductNameView;
    private TextView mPriceView;
    private Button mSingleSubmitButton;
    private View mFooterView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fcode_select_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mFooterView = LayoutInflater.from(getActivity()).inflate(R.layout.fcode_list_footer, null, false);
        mSubmitButton = (Button) mFooterView.findViewById(R.id.submit_btn);
        mLisContainer = view.findViewById(R.id.list_container);
        mSingleContainer = view.findViewById(R.id.single_container);
        mProductNameView = (TextView) view.findViewById(R.id.name);
        mPriceView = (TextView) view.findViewById(R.id.price);
        mImage = (ImageView) view.findViewById(R.id.photo);
        int hight = Device.DISPLAY_WIDTH - Device.DISPLAY_DENSITY * 20 / 160;
        mImage.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, hight));
        mSingleSubmitButton = (Button) view.findViewById(R.id.single_submit_btn);
        mAdapter = new FCodeSelectAdapter(getActivity());
        mListView.addFooterView(mFooterView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mItemClickListener);
        mLoadingView.setEmptyText(R.string.fcode_select_err);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String json = bundle.getString(Constants.Intent.EXTRA_CHECKCODE_LISTSTR);
            if (!TextUtils.isEmpty(json)) {
                ArrayList<FcodeSelectProduct> list = FcodeSelectProduct.valueOf(json);
                mAdapter.updateData(list);
                if (list != null && list.size() == 1) {
                    mLisContainer.setVisibility(View.GONE);
                    mSingleContainer.setVisibility(View.VISIBLE);
                    ImageLoader.getInstance().loadImage(mImage, list.get(0).getSingleImage(),
                            R.drawable.default_pic_small);
                    mProductNameView.setText(list.get(0).getName());
                    mPriceView.setText(getString(R.string.rmb_identification,list.get(0).getPrice()));
                    mCheckedProductId = list.get(0).getProductId();
                } else {
                    mLisContainer.setVisibility(View.VISIBLE);
                    mSingleContainer.setVisibility(View.GONE);
                }
            }
        }

        Utils.SoftInput.hide(getActivity(), view.getWindowToken());

        mSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View btn) {
                sendAddShoppingRequest();
            }
        });
        mSingleSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View btn) {
                sendAddShoppingRequest();
            }
        });
        if (mAdapter.getCount() > 0) {
            mAdapter.setCheckedPosition(0);
            mCheckedProductId = ((FcodeSelectProduct) mAdapter.getItem(0)).getProductId();
        }
    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
            mAdapter.setCheckedPosition(position);
            mCheckedProductId = (String) item.getTag();
        }
    };

    private void sendAddShoppingRequest() {
        getLoaderManager().initLoader(REQUEST_LOADER, null, mRequestCallback);
        Request request = new Request(HostManager.getAddShopping());
        request.addParam(Parameters.Keys.PRODUCT_ID, mCheckedProductId);
        request.addParam(Parameters.Keys.SOURCE, Parameters.Values.SOURCE_FCODE);
        mLoader.load(REQUEST_CODE_ADD_SHOPPING, request);
        mProgressDialog = ProgressDialog.show(getActivity(), null,
                getString(R.string.shake_adding_cart), false, true);
    }

    private void onAddShoppingSuccess() {
        mProgressDialog.dismiss();
        // 跳转到购物车
        Intent intent = new Intent(getActivity(), ShoppingActivity.class);
        startActivity(intent);
    }

    private void onAddShoppingError(String msg) {
        mProgressDialog.dismiss();
        ToastUtil.show(getActivity(), msg);
    }

    private LoaderCallbacks<RequestLoader.Result> mRequestCallback = new LoaderCallbacks<RequestLoader.Result>() {
        @Override
        public Loader<Result> onCreateLoader(int id, Bundle bundle) {
            mLoader = new RequestLoader(getActivity());
            return mLoader;
        }

        @Override
        public void onLoadFinished(Loader<Result> loader, Result data) {
            // 每一次调用结束，必须destroyLoader
            getLoaderManager().destroyLoader(REQUEST_LOADER);
            if (data != null) {
                if (REQUEST_CODE_ADD_SHOPPING == data.mRequestCode) {
                    if (data.mStatus == Request.STATUS_OK) {
                        ActionResult result = ActionResult.valueOf(data.mData);
                        if (Tags.RESULT_OK.equals(result.getMessage())) {
                            onAddShoppingSuccess();
                        } else {
                            onAddShoppingError(result.getMessage());
                        }
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Result> loader) {
        }
    };
}
