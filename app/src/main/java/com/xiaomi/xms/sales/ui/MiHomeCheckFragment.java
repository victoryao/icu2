
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.MiHomeBuyActivity;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.MihomeCheckLoader;
import com.xiaomi.xms.sales.loader.RequestLoader;
import com.xiaomi.xms.sales.loader.RequestLoader.Result;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.MiHomeCheckInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.SelfBindView;
import com.xiaomi.xms.sales.widget.SelfBindView.SelfBindViewInteface;
import com.xiaomi.xms.sales.zxing.ScannerActivity;

import org.json.JSONException;

public class MiHomeCheckFragment extends BaseFragment implements
        LoaderCallbacks<MihomeCheckLoader.Result> {

    public static final int MIHOMELOADER = 0;
    public static final int MIHOMECHECKINLOADER = 1;

    private Bundle mBundle;
    private String mMihomeId;
    private String mClientMihomeId;
    private Button mMihomeCheckView;
    private Button mMihomeBuyProductView;
    private TextView mMihomeNameView;
    private TextView mMihomeCheckCountView;
    private LinearLayout mMihomeLogoContainer;
    private SelfBindView mMihomeLogo;
    private View mMihomeBg;
    private View mContainer;
    private EmptyLoadingView mLoadingView;

    private MiHomeCheckInfo mMiHomeCheckInfo;
    private RequestLoader mCheckInLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mihome_check_fragment, container, false);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mContainer = view.findViewById(R.id.container);
        mMihomeBg = view.findViewById(R.id.mihome_bg_color);
        mMihomeNameView = (TextView) view.findViewById(R.id.mihome_name);
        mMihomeCheckCountView = (TextView) view.findViewById(R.id.mihomecheck_count);
        mMihomeCheckView = (Button) view.findViewById(R.id.mihome_check_btn);
        mMihomeCheckView.setOnClickListener(mClickListener);
        mMihomeBuyProductView = (Button) view.findViewById(R.id.mihome_buy_btn);
        mMihomeBuyProductView.setOnClickListener(mClickListener);
        mMihomeLogoContainer = (LinearLayout) view.findViewById(R.id.container_logo);
        initImageContainer();
        mBundle = getArguments();
        if (mBundle != null) {
            mMihomeId = mBundle.getString(Constants.Intent.EXTRA_MIHOME_BUY);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mLoader == null) {
            getLoaderManager().initLoader(MIHOMELOADER, null, this);
            mLoader.setNeedDatabase(false);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle arg1) {
        if (id == MIHOMELOADER) {
            mLoader = new MihomeCheckLoader(getActivity(), mMihomeId);
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }
        // mLoader.setProgressNotifiable(mLoadingView);
        return null;
    }

    private void initImageContainer() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final LinearLayout parentView = (LinearLayout) layoutInflater.inflate(R.layout.selfbind_container,
                null);
        mMihomeLogo = (SelfBindView) parentView.findViewById(R.id.selfbind_image);
        mMihomeLogo.SelfBindViewCallBack = new SelfBindViewInteface() {
            @Override
            public void bindView(ImageView view, Bitmap bitmap, Image image) {
                parentView.setLayoutParams(new LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT, bitmap.getHeight()
                                * Device.DISPLAY_WIDTH / bitmap.getWidth()));
                view.setImageBitmap(image.proccessImage(bitmap));
            }
        };
        mMihomeLogoContainer.addView(parentView);
    }

    @Override
    public void onLoadFinished(Loader<MihomeCheckLoader.Result> loader,
            MihomeCheckLoader.Result data) {
        mMiHomeCheckInfo = data.mMiHomeCheckInfo;

        if (mMiHomeCheckInfo != null) {
            mClientMihomeId = mMiHomeCheckInfo.getMihomeId();
            mContainer.setVisibility(View.VISIBLE);
            String color = TextUtils.isEmpty(mMiHomeCheckInfo.getColor()) ? "#fff05000" : mMiHomeCheckInfo.getColor();
            mMihomeBg.setBackgroundColor(Color.parseColor(color));
            mMihomeNameView.setText(mMiHomeCheckInfo.getMihomeName());
            mMihomeCheckCountView.setText(getString(R.string.mihome_check_count,
                    mMiHomeCheckInfo.getCheckInCount()));
            mMihomeCheckCountView.setTextColor(Color.argb(155, 0, 0, 0)); // 文字透明度
            ImageLoader.getInstance().loadImage(mMihomeLogo, mMiHomeCheckInfo.getImage(),
                    R.drawable.default_pic_large);
        } else {
            Bundle bundel = new Bundle();
            bundel.putString(Constants.Intent.EXTRA_MIHOME_ERROR_RESULT, mMihomeId);
            ((BaseActivity)getActivity()).showFragment(MiHomeBuyActivity.TAG_MIHOME_BUY_ERROR_FRAGMENT, bundel, false);
        }
    }

    @Override
    public void onLoaderReset(Loader<MihomeCheckLoader.Result> arg0) {
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mihome_check_btn:
                    mihomeCheckIn();
                    break;
                case R.id.mihome_buy_btn:
                    gotoScannerProduct();
                    break;
                default:
                    break;
            }
        }
    };

    private void gotoScannerProduct() {
        Intent intent = new Intent(getActivity(), ScannerActivity.class);
        intent.putExtra(Constants.Intent.EXTRA_MIHOME_BUY, mClientMihomeId);
        intent.setAction(Constants.Intent.ACTION_PRODUCT_SCANNER);
        startActivity(intent);
    }

    private void mihomeCheckIn() {
        getLoaderManager().initLoader(MIHOMECHECKINLOADER, null, mCheckInCallback);
        if (mCheckInLoader != null) {
            Request request = new Request(HostManager.getMihomeSignin());
            request.addParam(Parameters.Keys.MIHOME_ID, mClientMihomeId);
            mCheckInLoader.load(MIHOMECHECKINLOADER, request);
        }
    }

    private LoaderCallbacks<RequestLoader.Result> mCheckInCallback = new LoaderCallbacks<RequestLoader.Result>() {

        @Override
        public Loader<Result> onCreateLoader(int id, Bundle arg1) {
            mCheckInLoader = new RequestLoader(getActivity());
            return mCheckInLoader;
        }

        @Override
        public void onLoadFinished(Loader<Result> loader, Result result) {
            getLoaderManager().destroyLoader(MIHOMECHECKINLOADER);
            if (result != null) {
                if (Tags.isJSONResultOK(result.mData)) {
                    try {
                        String count = result.mData.getJSONObject(Tags.DATA).getString(
                                Tags.MihomeCheckInfo.SIGNIN_COUNT);
                        if (!TextUtils.isEmpty(count)) {
                            mMihomeCheckCountView.setText(getString(R.string.mihome_check_count,
                                    count));
                            ToastUtil.show(getActivity(), R.string.mihome_checkin_success);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastUtil.show(getActivity(), R.string.mihome_already_checkin);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Result> arg0) {
        }
    };

}
