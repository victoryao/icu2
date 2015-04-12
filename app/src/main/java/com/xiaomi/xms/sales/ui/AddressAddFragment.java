
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.activity.AddressActivity;
import com.xiaomi.xms.sales.adapter.RegionAdapter;
import com.xiaomi.xms.sales.cache.RegionCache;
import com.xiaomi.xms.sales.cache.RegionCache.QueryCallback;
import com.xiaomi.xms.sales.db.DBContract.Region;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils.PhoneFormat;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;

public class AddressAddFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "AddressAddFragment";
    private static final int UNSELECTED = 0;

    private EditText mConsignee;
    private Spinner mProvinceSpinner;
    private Spinner mCitySpinner;
    private Spinner mDistrictSpinner;
    private EditText mLocation;
    private EditText mZipCode;
    private EditText mTel;
    private Button mSubmit;

    private RegionAdapter mProvinceAdapter = null;
    private RegionAdapter mCityAdapter = null;
    private RegionAdapter mDistrictAdapter = null;

    private ProgressDialog mProgressDialog;
    private String mAction;

    private String mOldAddressId = null;
    private String mOldConsignee = "";
    private String mOldLocation = "";
    private String mOldZipCode = "";
    private String mOldTel = "";
    private String mFormatTel = "";
    private int mOldProvinceId = -1;
    private int mOldCityId = -1;
    private int mOldDistrictId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.address_add_fragment, container, false);
        mConsignee = (EditText) view.findViewById(R.id.address_consignee);
        mProvinceSpinner = (Spinner) view.findViewById(R.id.address_province);
        mCitySpinner = (Spinner) view.findViewById(R.id.address_city);
        mDistrictSpinner = (Spinner) view.findViewById(R.id.address_district);
        mLocation = (EditText) view.findViewById(R.id.address_location);
        mZipCode = (EditText) view.findViewById(R.id.address_zipcode);
        mTel = (EditText) view.findViewById(R.id.address_tel);
        mSubmit = (Button) view.findViewById(R.id.address_submit);
        mTel.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if(mTel.getText().toString().contains("*")) {
                        mTel.setText("");
                    }
                }
            }
        });
        setTabIndex();

        return view;
    }

    public boolean onBackPressed() {
        // get input
        String consignee = mConsignee.getText().toString();
        int province = (Integer) mProvinceSpinner.getSelectedView().getTag();
        int city = (Integer) mCitySpinner.getSelectedView().getTag();
        int district = (Integer) mDistrictSpinner.getSelectedView().getTag();
        String location = mLocation.getText().toString();
        String zipCode = mZipCode.getText().toString();
        String tel = mTel.getText().toString();

        if (modified(consignee, province, city, district, location, zipCode, tel)) {
            comfirmDialog();
            return true;
        }
        return false;
    }

    public void setAction(String action) {
        mAction = action;
    }

    private void comfirmDialog() {
        BaseAlertDialog dialog = new BaseAlertDialog(getActivity());
        dialog.setMessage(R.string.tips_modified);
        dialog.setPositiveButton(R.string.dialog_ask_ok, this);
        dialog.setNegativeButton(R.string.dialog_ask_cancel, this);
        dialog.show();
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

    private void loadOldAddressInfo(Bundle bundle) {
        mOldAddressId = bundle.getString(Constants.Intent.EXTRA_ADDRESS_ID);
        mOldConsignee = bundle.getString(Constants.Intent.EXTRA_ADDRESS_CONSIGNEE);
        mOldLocation = bundle.getString(Constants.Intent.EXTRA_ADDRESS_LOCATION);
        mOldZipCode = bundle.getString(Constants.Intent.EXTRA_ADDRESS_ZIPCODE);
        mOldTel = bundle.getString(Constants.Intent.EXTRA_ADDRESS_TEL);
        mOldProvinceId = bundle.getInt(Constants.Intent.EXTRA_ADDRESS_PROVINCE);
        mOldCityId = bundle.getInt(Constants.Intent.EXTRA_ADDRESS_CITY);
        mOldDistrictId = bundle.getInt(Constants.Intent.EXTRA_ADDRESS_DISTRICT);
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            mProvinceSpinner.setOnItemSelectedListener(mProvinceListener);
            mCitySpinner.setOnItemSelectedListener(mCityListener);
            mDistrictSpinner.setOnItemSelectedListener(mDistrictListener);
            return;
        }
        loadOldAddressInfo(bundle);

        mFormatTel = PhoneFormat.valueOf(mOldTel);
        mConsignee.setText(mOldConsignee);
        mLocation.setText(mOldLocation);
        mZipCode.setText(mOldZipCode);
        mTel.setText(mFormatTel);

        // initial province
        mProvinceSpinner
                .setSelection(findPositionInCursor(mProvinceAdapter.getCursor(), mOldProvinceId));

        // get city list and initial city
        RegionCache.getInstance(getActivity()).getCityByProvinceId(
                mOldProvinceId, new QueryCallback() {
                    @Override
                    public void queryComplete(Cursor cursor) {
                        cursor = prependHeader(cursor, getString(R.string.address_city_select));
                        if (mCityAdapter == null) {
                            mCityAdapter = new RegionAdapter(getActivity(), cursor);
                        }
                        mCitySpinner.setAdapter(mCityAdapter);
                        mCitySpinner.setSelection(findPositionInCursor(cursor, mOldCityId));
                        mCitySpinner.setEnabled(true);
                        mProvinceSpinner.setOnItemSelectedListener(mProvinceListener);

                        // get district list and init
                        RegionCache.getInstance(getActivity()).getDistrictByCityId(
                                mOldCityId, new QueryCallback() {
                                    @Override
                                    public void queryComplete(Cursor cursor) {
                                        cursor = prependHeader(cursor,
                                                getString(R.string.address_district_select));
                                        if (mDistrictAdapter == null) {
                                            mDistrictAdapter = new RegionAdapter(getActivity(),
                                                    cursor);
                                        }
                                        mDistrictSpinner.setAdapter(mDistrictAdapter);
                                        mDistrictSpinner.setSelection(findPositionInCursor(cursor,
                                                mOldDistrictId));
                                        mDistrictSpinner.setEnabled(true);
                                        mCitySpinner.setOnItemSelectedListener(mCityListener);
                                        mDistrictSpinner
                                                .setOnItemSelectedListener(mDistrictListener);
                                    }
                                });
                    }
                });
        LogUtil.d(TAG, "initData");
    }

    private int findPositionInCursor(Cursor cursor, int id) {
        if (cursor.moveToFirst()) {
            do {
                if (id == cursor.getInt(cursor.getColumnIndex(Region._ID))) {
                    return cursor.getPosition();
                }
            } while (cursor.moveToNext());
        }
        return -1;
    }

    private QueryCallback mGetProvinceOnCreateCallback = new QueryCallback() {
        @Override
        public void queryComplete(Cursor cursor) {
            if (cursor == null) {
                LogUtil.w(TAG, "get province null.");
            } else {
                if (cursor.getCount() == 0) {
                    cursor.close();
                    return;
                }
                LogUtil.d(TAG, "get province ok.");
                mProvinceAdapter = new RegionAdapter(getActivity(), prependHeader(
                        cursor, getString(R.string.address_province_select)));
                mProvinceSpinner.setAdapter(mProvinceAdapter);
                initData();
            }
        }
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RegionCache.getInstance(getActivity()).getProvince(mGetProvinceOnCreateCallback);
        LogUtil.d(TAG, "received addressid:" + mOldAddressId);
        mSubmit.setOnClickListener(mSubmitListener);
        mCitySpinner.setEnabled(false);
        mDistrictSpinner.setEnabled(false);
    }

    private Cursor prependHeader(Cursor cursor, String name) {
        MatrixCursor header = new MatrixCursor(new String[] {
                Region._ID, Region.NAME
        });
        header.addRow(new Object[] {
                -1, name
        });
        if (cursor == null) {
            return header;
        }
        return new MergeCursor(new Cursor[] {
                header, cursor
        });
    }

    private OnItemSelectedListener mProvinceListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.d(TAG, "province selected:" + id);
            if (id > 0) {
                mCitySpinner.setEnabled(true);
            } else {
                mCitySpinner.setEnabled(false);
            }
            mCitySpinner.setSelection(UNSELECTED);
            mDistrictSpinner.setEnabled(false);
            RegionCache.getInstance(getActivity()).getCityByProvinceId(
                    id, new QueryCallback() {
                        @Override
                        public void queryComplete(Cursor cursor) {
                            cursor = prependHeader(cursor, getString(R.string.address_city_select));
                            if (mCityAdapter == null) {
                                LogUtil.d(TAG, "new city adapter");
                                mCityAdapter = new RegionAdapter(getActivity(), cursor);
                                mCitySpinner.setAdapter(mCityAdapter);
                            } else {
                                mCityAdapter.changeCursor(cursor);
                            }
                        }
                    });
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private OnItemSelectedListener mCityListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.d(TAG, "city selected:" + id);
            if (id > 0) {
                mDistrictSpinner.setEnabled(true);
            } else {
                mDistrictSpinner.setEnabled(false);
            }
            mDistrictSpinner.setSelection(UNSELECTED);
            RegionCache.getInstance(getActivity()).getDistrictByCityId(id,
                    new QueryCallback() {
                        @Override
                        public void queryComplete(Cursor cursor) {
                            cursor = prependHeader(cursor,
                                    getString(R.string.address_district_select));
                            if (mDistrictAdapter == null) {
                                LogUtil.d(TAG, "new district adapter");
                                mDistrictAdapter = new RegionAdapter(getActivity(), cursor);
                                mDistrictSpinner.setAdapter(mDistrictAdapter);
                            } else {
                                mDistrictAdapter.changeCursor(cursor);
                            }
                        }
                    });
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private OnItemSelectedListener mDistrictListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.d(TAG, "district selected:" + id);
            if (!TextUtils.isEmpty(mOldZipCode)) {
                return;
            }
            if (id <= 0) {
                mZipCode.setText("");
                return;
            }
            RegionCache.getInstance(getActivity()).getZipCodeById(id,
                    new QueryCallback() {
                        @Override
                        public void queryComplete(Cursor cursor) {
                            if (cursor != null) {
                                try {
                                    cursor.moveToFirst();
                                    mZipCode.setText(String.format(
                                            getResources().getString(R.string.zipcode_format),
                                            cursor.getInt(cursor
                                                    .getColumnIndex(Region.ZIPCODE))));
                                } finally {
                                    cursor.close();
                                }
                            }
                        }
                    });
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private boolean modified(String consignee, int provinceId, int cityId, int districtId,
            String location, String zipcode, String tel) {
        if (!TextUtils.equals(consignee, mOldConsignee)) {
            return true;
        }
        if (provinceId != mOldProvinceId) {
            return true;
        }
        if (cityId != mOldCityId) {
            return true;
        }
        if (districtId != mOldDistrictId) {
            return true;
        }
        if (!TextUtils.equals(location, mOldLocation)) {
            return true;
        }
        if (!TextUtils.equals(zipcode, mOldZipCode)) {
            return true;
        }
        if (!TextUtils.equals(tel, mOldTel)) {
            return true;
        }
        return false;
    }

    private void sendRequestIntent(String consignee, int provinceId, int cityId, int districtId,
            String location, String zipcode, String tel) {
        Intent intent = new Intent(getActivity(), ShopIntentService.class);
        if (mOldAddressId == null) {
            mProgressDialog = ProgressDialog.show(getActivity(),
                    getString(R.string.address_adding), getString(R.string.address_adding));
            intent.setAction(Constants.Intent.ACTION_ADD_ADDRESS);
        } else {
            mProgressDialog = ProgressDialog.show(getActivity(),
                    getString(R.string.address_saving), getString(R.string.address_saving));
            intent.setAction(Constants.Intent.ACTION_EDIT_ADDRESS);
            intent.putExtra(Constants.Intent.EXTRA_ADDRESS_ID, mOldAddressId);
        }
        mProgressDialog.setCancelable(true);
        intent.putExtra(Constants.Intent.EXTRA_ADDRESS_CONSIGNEE, consignee);
        intent.putExtra(Constants.Intent.EXTRA_ADDRESS_PROVINCE, provinceId);
        intent.putExtra(Constants.Intent.EXTRA_ADDRESS_CITY, cityId);
        intent.putExtra(Constants.Intent.EXTRA_ADDRESS_DISTRICT, districtId);
        intent.putExtra(Constants.Intent.EXTRA_ADDRESS_LOCATION, location);
        intent.putExtra(Constants.Intent.EXTRA_ADDRESS_ZIPCODE, zipcode);
        intent.putExtra(Constants.Intent.EXTRA_ADDRESS_TEL, PhoneNumberUtils.stripSeparators(tel));
        getActivity().startService(intent);
    }

    private boolean checkInput(String consignee, int provinceId, int cityId, int districtId,
            String location, String zipcode, String tel) {
        if (TextUtils.isEmpty(consignee)) {
            ToastUtil.show(getActivity(), R.string.tips_address_consignee);
            mConsignee.requestFocus();
            return false;
        }
        if (provinceId <= 0) {
            ToastUtil.show(getActivity(), R.string.tips_address_province);
            return false;
        }
        if (cityId <= 0) {
            ToastUtil.show(getActivity(), R.string.tips_address_city);
            return false;
        }
        if (districtId <= 0) {
            ToastUtil.show(getActivity(), R.string.tips_address_district);
            return false;
        }
        if (TextUtils.isEmpty(location)) {
            mLocation.requestFocus();
            ToastUtil.show(getActivity(), R.string.tips_address_location);
            return false;
        }
        if (TextUtils.isEmpty(zipcode)) {
            mZipCode.requestFocus();
            ToastUtil.show(getActivity(), R.string.tips_address_zipcode);
            return false;
        }
        if (zipcode.length() != 6) {
            mZipCode.requestFocus();
            ToastUtil.show(getActivity(), R.string.tips_address_zipcode_length);
            return false;
        }
        if (TextUtils.isEmpty(tel)) {
            mTel.requestFocus();
            ToastUtil.show(getActivity(), R.string.tips_address_tel);
            return false;
        }
        return true;
    }

    private OnClickListener mSubmitListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // get input
            String consignee = mConsignee.getText().toString();
            int province = (Integer) mProvinceSpinner.getSelectedView().getTag();
            int city = (Integer) mCitySpinner.getSelectedView().getTag();
            int district = (Integer) mDistrictSpinner.getSelectedView().getTag();
            String location = mLocation.getText().toString();
            String zipCode = mZipCode.getText().toString();
            String tel = mTel.getText().toString();
            if(TextUtils.equals(mFormatTel, tel)) {
                tel = mOldTel;
            }

            if (!modified(consignee, province, city, district, location, zipCode, tel)) {
                // no modify, return to last UI
                LogUtil.d(TAG, "log not modify!");
                ((AddressActivity) getActivity()).onBackPressed(true);
                return;
            }

            // check input fomat
            if (!checkInput(consignee, province, city, district, location, zipCode, tel)) {
                return;
            }

            sendRequestIntent(consignee, province, city, district, location, zipCode, tel);
        }
    };

    private void errorMsg(String error, int errorId) {
        if (TextUtils.isEmpty(error)) {
            ToastUtil.show(getActivity(), R.string.address_err);
        } else {
            ToastUtil.show(getActivity(), error);
        }
        switch (errorId) {
            case Tags.AddressInfo.ERROR_CODE_CONSIGNEE:
                mConsignee.requestFocus();
                break;
            case Tags.AddressInfo.ERROR_CODE_LOCATION:
                mLocation.requestFocus();
                break;
            case Tags.AddressInfo.ERROR_CODE_ZIPCODE:
                mZipCode.requestFocus();
                break;
            case Tags.AddressInfo.ERROR_CODE_TEL:
                mTel.requestFocus();
                break;
        }
    }

    public void addAddressComplete(int result, String error, int errorId, String newAddressId) {
        LogUtil.d(TAG, "add address complete:" + result);
        mProgressDialog.dismiss();
        switch (result) {
            case Request.STATUS_OK:
                if (Constants.Intent.ACTION_EDIT_ADDRESS.equals(mAction)) {
                    AddressActivity parent = (AddressActivity) getActivity();
                    parent.setAddressListReload(true);
                    parent.onBackPressed(true);
                } else {
                    Intent i = new Intent();
                    i.putExtra(Constants.Intent.EXTRA_ADDRESS_ID, newAddressId);
                    getActivity().setResult(Activity.RESULT_OK, i);
                    getActivity().finish();
                }
                LogUtil.d(TAG, "ok");
                break;
            case Request.STATUS_NETWORK_UNAVAILABLE:
                ToastUtil.show(getActivity(), R.string.network_unavaliable);
                break;
            default:
                errorMsg(error, errorId);
        }
    }

    public void editAddressComplete(int result, String error, int errorId) {
        LogUtil.d(TAG, "edit address complete:" + result);
        mProgressDialog.dismiss();
        switch (result) {
            case Request.STATUS_OK:
                AddressActivity parent = (AddressActivity) getActivity();
                parent.setAddressListReload(true);
                parent.onBackPressed(true);
                break;
            case Request.STATUS_NETWORK_UNAVAILABLE:
                ToastUtil.show(getActivity(), R.string.network_unavaliable);
                break;
            default:
                errorMsg(error, errorId);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == BaseAlertDialog.BUTTON_POSITIVE) {
            ((AddressActivity) getActivity()).onBackPressed(true);
        } else if (v.getId() == BaseAlertDialog.BUTTON_NEGATIVE) {

        }
    }
}
