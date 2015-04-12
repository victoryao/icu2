
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.MessageCheckActivity;
import com.xiaomi.xms.sales.activity.OrderEditActivity;
import com.xiaomi.xms.sales.loader.OrderInfoLoader;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils.PhoneFormat;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class OrderEditTypeFragment extends BaseFragment {
    private static final int ORDER_INFO_LOADER = 0;

    private OrderInfoLoader mLoader;
    private View mAddressView;
    private View mDeliverTimeView;
    private View mContainer;
    private View mAddressImageView;
    private View mTimeImageView;
    private TextView mAddressUp;
    private TextView mAddressMiddle;
    private TextView mAddressBottom;
    private EmptyLoadingView mLoadingView;

    private String mOrderId = "";
    private String mOldConsignee = "";
    private String mOldLocation = "";
    private String mOldZipCode = "";
    private String mOldTel = "";
    private int mOldProvinceId = -1;
    private int mOldCityId = -1;
    private int mOldDistrictId = -1;
    private String mOldDeliverTime;
    private boolean hasPhone;
    private boolean isMeassageCheck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_edit_type_fragment, container, false);
        mContainer = view.findViewById(R.id.edit_container);
        mAddressView = view.findViewById(R.id.edit_address);
        mDeliverTimeView = view.findViewById(R.id.edit_time);
        mDeliverTimeView.setBackgroundResource(R.drawable.list_item_single_bg);
        mAddressImageView = view.findViewById(R.id.address_right_arrow);
        mTimeImageView = view.findViewById(R.id.time_arrow);
        mAddressUp = (TextView) view.findViewById(R.id.checkout_address_up);
        mAddressMiddle = (TextView) view.findViewById(R.id.checkout_address_middle);
        mAddressBottom = (TextView) view.findViewById(R.id.checkout_address_bottom);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrderId = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            isMeassageCheck = bundle.getBoolean(Constants.Intent.EXTRA_EDIT_ORDER_MESSAGE_CHECK);
        }
        getLoaderManager().initLoader(ORDER_INFO_LOADER, null, mOrderInfoCallback);

        mAddressView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMeassageCheck) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.Intent.EXTRA_ADDRESS_CONSIGNEE, mOldConsignee);
                    bundle.putInt(Constants.Intent.EXTRA_ADDRESS_PROVINCE, mOldProvinceId);
                    bundle.putInt(Constants.Intent.EXTRA_ADDRESS_CITY, mOldCityId);
                    bundle.putInt(Constants.Intent.EXTRA_ADDRESS_DISTRICT, mOldDistrictId);
                    bundle.putString(Constants.Intent.EXTRA_ADDRESS_LOCATION, mOldLocation);
                    bundle.putString(Constants.Intent.EXTRA_ADDRESS_ZIPCODE, mOldZipCode);
                    bundle.putString(Constants.Intent.EXTRA_ADDRESS_TEL, mOldTel);
                    bundle.putString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                    bundle.putBoolean(Constants.Intent.EXTRA_ORDER_HAS_PHONE, hasPhone);
                    ((OrderEditActivity) getActivity()).showFragment(
                            OrderEditActivity.TAG_EDIT_ADDRESS_FRAGMENT, bundle, true);
                } else {
                    Intent intent = new Intent(getActivity(), MessageCheckActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_ORDER_EDIT_OLDTEL, mOldTel);
                    intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                    startActivityForResult(intent, Constants.RequestCode.CODE_REQUEST_EDIT_ORDER);
                }

            }
        });

        mDeliverTimeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.Intent.EXTRA_ORDER_DELIVER_TIME, mOldDeliverTime);
                bundle.putString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                ((OrderEditActivity) getActivity()).showFragment(
                        OrderEditActivity.TAG_EDIT_DELIVERTIME_FRAGMENT, bundle, true);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.RequestCode.CODE_REQUEST_EDIT_ORDER:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.Intent.EXTRA_ADDRESS_CONSIGNEE, mOldConsignee);
                    bundle.putInt(Constants.Intent.EXTRA_ADDRESS_PROVINCE, mOldProvinceId);
                    bundle.putInt(Constants.Intent.EXTRA_ADDRESS_CITY, mOldCityId);
                    bundle.putInt(Constants.Intent.EXTRA_ADDRESS_DISTRICT, mOldDistrictId);
                    bundle.putString(Constants.Intent.EXTRA_ADDRESS_LOCATION, mOldLocation);
                    bundle.putString(Constants.Intent.EXTRA_ADDRESS_ZIPCODE, mOldZipCode);
                    bundle.putString(Constants.Intent.EXTRA_ADDRESS_TEL, mOldTel);
                    bundle.putString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                    bundle.putBoolean(Constants.Intent.EXTRA_ORDER_HAS_PHONE, hasPhone);
                    ((OrderEditActivity) getActivity()).showFragment(
                            OrderEditActivity.TAG_EDIT_ADDRESS_FRAGMENT, bundle, true);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLoader != null && !mLoader.isLoading()) {
            mLoader.forceLoad();
        }
        getActivity().setTitle(R.string.order_edit_type_title);
    }

    private LoaderCallbacks<OrderInfoLoader.Result> mOrderInfoCallback = new LoaderCallbacks<OrderInfoLoader.Result>() {
        @Override
        public Loader onCreateLoader(int id, Bundle arg1) {
            if (id == ORDER_INFO_LOADER) {
                mLoader = new OrderInfoLoader(getActivity());
                mLoader.setNeedSecurityKeyTask(false);
                mLoader.setOrderId(mOrderId);
                mLoader.setProgressNotifiable(mLoadingView);
                mLoader.setNeedDatabase(false);
                return mLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<OrderInfoLoader.Result> loader,
                OrderInfoLoader.Result data) {
            if (data.mOrderInfo != null) {
                mContainer.setVisibility(View.VISIBLE);
                mOldConsignee = data.mOrderInfo.getConsignee();
                mOldLocation = data.mOrderInfo.getConsigneeAddress();
                mOldZipCode = data.mOrderInfo.getZipCode();
                mOldTel = data.mOrderInfo.getConsigneePhone();
                mOldProvinceId = data.mOrderInfo.getProvinceId();
                mOldCityId = data.mOrderInfo.getCityId();
                mOldDistrictId = data.mOrderInfo.getDistrictId();
                mOldDeliverTime = data.mOrderInfo.getDeliveryTime();
                hasPhone = data.mOrderInfo.hasPhone();
                boolean isFirstEditAddress = data.mOrderInfo.getNexts().contains(
                        "EDIT_ORDER_ADDRESS");
                boolean isFirstEditTime = data.mOrderInfo.getNexts().contains("EDIT_ORDER_TIME");
                String oTel = mOldTel;;
                if (!TextUtils.isEmpty(oTel)) {
                    oTel = oTel.substring(0, 3) + "****" + oTel.substring(7);
                }
                mAddressBottom.setText(getResources().getString(R.string.address_title,
                        data.mOrderInfo.getConsignee(),
                        oTel));
                mAddressUp.setText(getResources().getString(R.string.address_area,
                        data.mOrderInfo.getProvince(),
                        data.mOrderInfo.getCity(),
                        data.mOrderInfo.getDistrict()));
                mAddressMiddle.setText(getResources().getString(R.string.address_location,
                        data.mOrderInfo.getConsigneeAddress(),
                        data.mOrderInfo.getZipCode()));
                /*
                 * ((TextView)
                 * mTelView.findViewById(R.id.tel_item_text)).setText
                 * (mOldTel.substring(0, 3) + "****" + mOldTel.substring(7));
                 */
                ((TextView) mDeliverTimeView.findViewById(R.id.time_item_text))
                        .setText(data.mOrderInfo.getDeliveryTime());
                if (isFirstEditAddress) {
                    mAddressView.setClickable(true);
                    mAddressImageView.setVisibility(View.VISIBLE);
                } else {
                    mAddressView.setClickable(false);
                    mAddressImageView.setVisibility(View.GONE);
                }
                if (isFirstEditTime) {
                    mDeliverTimeView.setClickable(true);
                    mTimeImageView.setVisibility(View.VISIBLE);
                } else {
                    mDeliverTimeView.setClickable(false);
                    mTimeImageView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<OrderInfoLoader.Result> loader) {
        }
    };
}
