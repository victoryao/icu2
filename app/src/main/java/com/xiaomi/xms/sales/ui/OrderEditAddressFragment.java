
package com.xiaomi.xms.sales.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.cache.RegionCache;
import com.xiaomi.xms.sales.cache.RegionCache.QueryCallback;
import com.xiaomi.xms.sales.db.DBContract.Region;
import com.xiaomi.xms.sales.loader.RequestLoader;
import com.xiaomi.xms.sales.loader.RequestLoader.Result;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.RegionSelector;
import com.xiaomi.xms.sales.widget.RegionSelector.OnSelectedListener;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class OrderEditAddressFragment extends BaseFragment {
    private static final String TAG = "OrderEditAddressFragment";
    private static final int UNSELECTED = 0;
    private static final int REQUEST_LOADER = 0;

    private RequestLoader mLoader;
    private RegionSelector mRegionSelector;
    private EditText mNameView;
    private EditText mLocation;
    private EditText mZipCode;
    private EditText mTel;
    private Button mSubmit;

    private String mOldConsignee;
    private String mOldAddressId = null;
    private String mOldLocation = "";
    private String mOldZipCode = "";
    private String mOldTel = "";
    private int mOldProvinceId = -1;
    private int mOldCityId = -1;
    private int mOldDistrictId = -1;
    private String mOrderId;
    private boolean hasPhone;
    private String mNewName = "";
    private String mNewTel = "";
    private String mNewLocation = "";
    private String mNewZipCode = "";
    private int mNewProvinceId = -1;
    private int mNewCityId = -1;
    private int mNewDistrictId = -1;
    private String mHideTel="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_edit_address_fragment, container, false);
        mRegionSelector = (RegionSelector) view.findViewById(R.id.city_selector);
        mNameView = (EditText) view.findViewById(R.id.address_consignee);
        mLocation = (EditText) view.findViewById(R.id.address_location);
        mZipCode = (EditText) view.findViewById(R.id.address_zipcode);
        mTel = (EditText) view.findViewById(R.id.address_tel);
        mSubmit = (Button) view.findViewById(R.id.address_submit);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtil.d(TAG, "received addressid:" + mOldAddressId);
        mSubmit.setOnClickListener(mSubmitListener);
        setTabIndex();
        initData();
        mRegionSelector.setOnSelectedListener(new OnSelectedListener() {
            @Override
            public void onProvinceSelected(long id) {
            }

            @Override
            public void onCitySelected(long id) {
            }

            @Override
            public void onDistrictSelected(long id) {
                RegionCache.getInstance(getActivity()).getZipCodeById(id, new QueryCallback() {
                    @Override
                    public void queryComplete(Cursor cursor) {
                        if (cursor != null) {
                            try {
                                cursor.moveToFirst();
                                mZipCode.setText(String.format(
                                        getResources().getString(R.string.zipcode_format),
                                        cursor.getInt(cursor.getColumnIndex(Region.ZIPCODE))));
                            } finally {
                                cursor.close();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.order_edit_address_title);
    }

    private void setTabIndex() {
        mLocation.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                mZipCode.requestFocus();
                return true;
            }
        });
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        loadOldAddressInfo(bundle);
        mNameView.setText(mOldConsignee);
        mLocation.setText(mOldLocation);
        mZipCode.setText(mOldZipCode);
        mHideTel = mOldTel.substring(0, 3) + "****" + mOldTel.substring(7);
        mTel.setText(mHideTel);
        mRegionSelector.set(mOldProvinceId, mOldCityId, mOldDistrictId);
    }

    private OnClickListener mSubmitListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // get input
            mNewName = mNameView.getText().toString();
            mNewProvinceId = mRegionSelector.getProvince();
            mNewCityId = mRegionSelector.getCity();
            mNewDistrictId = mRegionSelector.getDistrict();
            mNewLocation = mLocation.getText().toString();
            mNewZipCode = mZipCode.getText().toString();
            mNewTel = mTel.getText().toString();

            // TODO
            if(TextUtils.equals(mNewTel, mHideTel)) {
                mNewTel = mOldTel;
            }
            if (TextUtils.isEmpty(mNewTel)) {
                ToastUtil.show(getActivity(), R.string.order_edit_tel_not_null);
                return;
            }

            requestEditOrder(mNewName, mNewTel, mNewProvinceId, mNewCityId, mNewDistrictId,
                    mNewLocation,
                    mNewZipCode);
        }
    };

    private void loadOldAddressInfo(Bundle bundle) {
            mOldConsignee = bundle.getString(Constants.Intent.EXTRA_ADDRESS_CONSIGNEE);
            mOldLocation = bundle.getString(Constants.Intent.EXTRA_ADDRESS_LOCATION);
            mOldZipCode = bundle.getString(Constants.Intent.EXTRA_ADDRESS_ZIPCODE);
            mOldTel = bundle.getString(Constants.Intent.EXTRA_ADDRESS_TEL);
            mOldProvinceId = bundle.getInt(Constants.Intent.EXTRA_ADDRESS_PROVINCE);
            mOldCityId = bundle.getInt(Constants.Intent.EXTRA_ADDRESS_CITY);
            mOldDistrictId = bundle.getInt(Constants.Intent.EXTRA_ADDRESS_DISTRICT);
            mOrderId = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            hasPhone = bundle.getBoolean(Constants.Intent.EXTRA_ORDER_HAS_PHONE, false);
    }

    private Handler mHandler = new Handler();
    private LoaderCallbacks<RequestLoader.Result> mRequestCallback = new LoaderCallbacks<RequestLoader.Result>() {
        @Override
        public void onLoaderReset(Loader<Result> loader) {
        }

        @Override
        public void onLoadFinished(Loader<Result> loader, Result result) {
            getLoaderManager().destroyLoader(REQUEST_LOADER);
            if (result != null) {
                if (Tags.isJSONResultOK(result.mData)) {
                    ToastUtil.show(getActivity(), R.string.order_edit_ok);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().onBackPressed();
                        }
                    });
                } else {
                    ToastUtil.show(getActivity(), result.mData.optString(Tags.DESCRIPTION));
                }
            }
        }

        @Override
        public Loader<Result> onCreateLoader(int type, Bundle bundle) {
            mLoader = new RequestLoader(getActivity());
            return mLoader;
        }
    };

    private void requestEditOrder(String name, String tel, int province, int city, int district,
            String location,
            String zipCode) {
        getLoaderManager().initLoader(REQUEST_LOADER, null, mRequestCallback);
        if (mLoader != null) {
            Request request = new Request(HostManager.getEditOrder());
            request.addParam(Tags.EditOrder.USER_ID, LoginManager.getInstance().getUserId());
            request.addParam(Tags.EditOrder.ORDER_ID, mOrderId);
            request.addParam(Tags.EditOrder.CONSIGNEE, name);
            request.addParam(Tags.EditOrder.TEL, tel);
            request.addParam(Tags.EditOrder.TYPE, Tags.EditOrder.VALUE_TYPE_ADDRESS);
            request.addParam(Tags.EditOrder.PROVINCE, String.valueOf(province));
            request.addParam(Tags.EditOrder.CITY, String.valueOf(city));
            request.addParam(Tags.EditOrder.DISTRICT, String.valueOf(district));
            request.addParam(Tags.EditOrder.ADDRESS, location);
            request.addParam(Tags.EditOrder.ZIPCODE, zipCode);
            mLoader.load(REQUEST_LOADER, request);
        }
    }
}
