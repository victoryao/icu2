
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.activity.AddressActivity;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.loader.CheckoutLoader;
import com.xiaomi.xms.sales.loader.CheckoutLoader.Result;
import com.xiaomi.xms.sales.loader.RegionPaymentLoader;
import com.xiaomi.xms.sales.model.CheckoutFormInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.ui.CheckoutFragment.RadioButtonInfo.Tag;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.util.Utils.PhoneFormat;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class CheckoutFragment extends BaseFragment implements
        LoaderCallbacks<CheckoutLoader.Result>, OnCheckedChangeListener {
    private final static String TAG = "CheckoutFragment";
    private final static int TAG_BACKPRESS_MESSAGE = 1;
    private CheckoutLoader mLoader;
    private final static int CHECKOUT_LOADER = 0;
    private final static int REGION_PAYMENT_LOADER = 1;
    private CheckoutFormInfo mFormInfo;
    private View mAddress;
    private View mView;
    private View mCheckoutInfoContainer;
    private TextView mAddressUp;
    private TextView mAddressMiddle;
    private TextView mAddressBottom;
    private TextView mNext;
    private EmptyLoadingView mLoadingView;
    private RadioGroup mPaymentGroup;
    private RadioGroup mShipmentGroup;
    private RadioGroup mDelivertimeGroup;
    private RadioGroup mInvoiceGroup;
    private EditText mInvoiceTitle;
    private ViewGroup mInvoiceTitleBg;
    private ViewGroup mCheckoutAddressEmpty;
    private View mCheckoutHintContainer;
    private TextView mCheckoutHint;
    private String mAddressId;
    private String mRegionId;
    private EditText mMihomeBuyAddr;
    private EditText mMihomeBuyTel;
    private EditText mMihomeBuyConsignee;
    private static final String mRowTitles[] = {
            Tags.CheckoutSubmit.PAYLIST, Tags.CheckoutSubmit.DELIVERTIME,
            Tags.CheckoutSubmit.INVOICE
    };
    private final static int GROUP_PAYMENT = 1;
    private final static int GROUP_SHIPMENT = 2;
    private final static int GROUP_DELIVEERTIME = 3;
    private final static int GROUP_INVOICE = 4;
    private final static int INVOICE_TITLE = 5;
    private RadioButton mInvoiceRadioButton = null;
    private HashMap<Integer, String> mFormDefaultValue = new HashMap<Integer, String>();
    private View mContainer;
    private boolean mIsPersonalInvoice;
    private String mMihomeBuyId = HostManager.Parameters.Values.MIHOME_BUY_NULL;
    private boolean mIsMihomeShopping = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** get mi-home buy id from bundle */
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMihomeBuyId = bundle.getString(Constants.Intent.EXTRA_MIHOME_BUY);
            if (mMihomeBuyId == null) {
                mMihomeBuyId = HostManager.Parameters.Values.MIHOME_BUY_NULL;
            }
            mIsMihomeShopping = !mMihomeBuyId.equals(HostManager.Parameters.Values.MIHOME_BUY_NULL);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getLoaderManager().getLoader(CHECKOUT_LOADER) == null) {
            mContainer.setVisibility(View.GONE);
            getLoaderManager().initLoader(CHECKOUT_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.checkout_fragment, container, false);
        mCheckoutInfoContainer = view.findViewById(R.id.checkout_info_container);
        mCheckoutHintContainer = view.findViewById(R.id.checkout_hint_container);
        mCheckoutHint = (TextView) view.findViewById(R.id.checkout_hint);
        mAddressUp = (TextView) view.findViewById(R.id.checkout_address_up);
        mAddressMiddle = (TextView) view.findViewById(R.id.checkout_address_middle);
        mAddressBottom = (TextView) view.findViewById(R.id.checkout_address_bottom);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mNext = (TextView) view.findViewById(R.id.next);
        mPaymentGroup = (RadioGroup) view.findViewById(R.id.checkout_form_radiogroup_payment);
        mPaymentGroup.setOnCheckedChangeListener(this);
        mShipmentGroup = (RadioGroup) view.findViewById(R.id.checkout_form_radiogroup_shipment);
        mShipmentGroup.setOnCheckedChangeListener(this);
        mDelivertimeGroup = (RadioGroup) view
                .findViewById(R.id.checkout_form_radiogroup_delivertime);
        mDelivertimeGroup.setOnCheckedChangeListener(this);
        mInvoiceGroup = (RadioGroup) view.findViewById(R.id.checkout_form_radiogroup_invoice);
        mInvoiceGroup.setOnCheckedChangeListener(this);
        mInvoiceTitle = (EditText) view.findViewById(R.id.checkout_form_invoice_title);
        mInvoiceTitleBg = (ViewGroup) view.findViewById(R.id.checkout_form_invoice_title_bg);
        mAddress = view.findViewById(R.id.checkout_address);
        mAddress.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openAddressActivity(mAddressId);
            }
        });
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onNext();
            }
        });
        mView = view;

        mCheckoutAddressEmpty = (ViewGroup) view.findViewById(R.id.checkout_address_empty);
        mCheckoutAddressEmpty.setVisibility(View.GONE);
        mCheckoutAddressEmpty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddressActivity.class);
                intent.setAction(Constants.Intent.ACTION_ADD_ADDRESS);
                startActivityForResult(intent, Constants.RequestCode.CODE_ADDRESS);
            }
        });
        getParent().getHomeButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        mContainer = view.findViewById(R.id.container);

        /**
         * If mi-home shopping, update UI.
         */
        mMihomeBuyAddr = (EditText) view.findViewById(R.id.checkout_addr_mihome);
        mMihomeBuyConsignee = (EditText) view.findViewById(R.id.checkout_addr_mihome_consignee);
        mMihomeBuyTel = (EditText) view.findViewById(R.id.checkout_addr_mihome_tel);
        if (mIsMihomeShopping) {
            /** no deliver time */
            View deliverTime = view.findViewById(R.id.checkout_form_delivertime);
            deliverTime.setVisibility(View.GONE);
            /** show mi-home address */
            View mihomeAddrLayout = view.findViewById(R.id.checkout_addr_mihome_layout);
            mihomeAddrLayout.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == CHECKOUT_LOADER) {
            mLoader = new CheckoutLoader(getActivity(), mMihomeBuyId);
            if (bundle != null) {
                String addressId = bundle.getString(Tags.CheckoutSubmit.ADDRESS_ID);
                mLoader.setAddressId(addressId);
            }
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> arg0, Result data) {
        LogUtil.d(TAG, "onLoadFinished");
        if (data.json == null || data.json.length() == 0 || !Tags.isJSONResultOK(data.json)) {
            // mCheckoutInfoContainer.setVisibility(View.GONE);
            String description = null;
            if (data.json != null) {
                description = data.json.optString(Tags.DESCRIPTION);
            }
            if (TextUtils.isEmpty(description)) {
                description = ShopApp.getContext().getString(R.string.data_error);
            }
            ToastUtil.show(ShopApp.getContext(), description);
            mHandler.sendEmptyMessage(TAG_BACKPRESS_MESSAGE);
            return;
        }
        mCheckoutInfoContainer.setVisibility(View.VISIBLE);
        removeFormData();
        setFormData(data.json);
        mContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

    private LoaderCallbacks<RegionPaymentLoader.Result> mRegionPaymentLoaderCallbacks = new LoaderCallbacks<RegionPaymentLoader.Result>() {

        @Override
        public Loader<com.xiaomi.xms.sales.loader.RegionPaymentLoader.Result> onCreateLoader(int id,
                Bundle bundle) {
            if (id == REGION_PAYMENT_LOADER) {
                String payId = bundle.getString(Tags.CheckoutSubmit.PAY_ID);
                RegionPaymentLoader loader = new RegionPaymentLoader(getActivity(), payId,
                        mRegionId, mMihomeBuyId);
                loader.setProgressNotifiable(mLoadingView);
                return loader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<com.xiaomi.xms.sales.loader.RegionPaymentLoader.Result> arg0,
                com.xiaomi.xms.sales.loader.RegionPaymentLoader.Result data) {
            if (data.json == null || data.json.length() == 0 || !Tags.isJSONResultOK(data.json)) {
                String description = null;
                if (data.json != null) {
                    description = data.json.optString(Tags.DESCRIPTION);
                }
                if (TextUtils.isEmpty(description)) {
                    description = ShopApp.getContext().getString(R.string.data_error);
                }
                ToastUtil.show(ShopApp.getContext(), description);
                return;
            }
            mShipmentGroup.removeAllViews();
            JSONObject dataJson = data.json.optJSONObject(Tags.DATA);
            if (dataJson != null) {
                JSONArray jArray;
                jArray = dataJson.optJSONArray(Tags.CheckoutSubmit.SHIPMENTLIST);
                if (jArray == null || jArray.length() == 0) {
                    return;
                }
                try {
                    setShipment(jArray);
                } catch (JSONException e) {
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<com.xiaomi.xms.sales.loader.RegionPaymentLoader.Result> data) {
        }
    };

    private UIHandler mHandler = new UIHandler(this);

    private static class UIHandler extends Handler {
        private final WeakReference<CheckoutFragment> mFragment;

        public UIHandler(CheckoutFragment fragment) {
            mFragment = new WeakReference<CheckoutFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TAG_BACKPRESS_MESSAGE) {
                CheckoutFragment ckfer = mFragment.get();
                if (ckfer != null)
                    ckfer.getActivity().onBackPressed();
            }
        }
    }

    private void setFormRow(String key, JSONArray jArray) {
        try {
            if (key.equals(Tags.CheckoutSubmit.PAYLIST)) {
                setPayment(jArray);
            } else if (key.equals(Tags.CheckoutSubmit.DELIVERTIME)) {
                setDelivertime(jArray);
            } else if (key.equals(Tags.CheckoutSubmit.INVOICE)) {
                setInvoice(jArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setDelivertime(JSONArray jArray) throws JSONException {
        RadioGroup radioGroup = mDelivertimeGroup;

        ArrayList<RadioButtonInfo> buttons = new ArrayList<RadioButtonInfo>();
        for (int i = 0; i < jArray.length(); i++) {
            RadioButtonInfo button = new RadioButtonInfo();
            JSONObject item = jArray.optJSONObject(i);
            button.mTitle = item.optString(Tags.CheckoutSubmit.DESC);
            button.mTag = item.optString(Tags.CheckoutSubmit.VALUE);
            buttons.add(button);
        }
        addRadioButtons(radioGroup, buttons, getDefaultValue(GROUP_DELIVEERTIME));
    }

    private void setInvoice(JSONArray jArray) throws JSONException {
        RadioGroup radioGroup = mInvoiceGroup;

        ArrayList<RadioButtonInfo> buttons = new ArrayList<RadioButtonInfo>();

        RadioButtonInfo button = new RadioButtonInfo();
        button.mTitle = getString(R.string.checkout_no_invoice);
        button.mTag = "0";
        buttons.add(button);
        for (int i = 0; i < jArray.length(); i++) {
            button = new RadioButtonInfo();
            JSONObject item = jArray.getJSONObject(i);
            button.mTitle = item.optString(Tags.CheckoutSubmit.DESC);
            button.mTag = item.optString(Tags.CheckoutSubmit.VALUE);
            buttons.add(button);
        }
        // INVOICE_ID_PERSONAL表示开个人发票
        if (mIsPersonalInvoice) {
            setDefaultValue(GROUP_INVOICE, Tags.CheckoutSubmit.INVOICE_ID_PERSONAL);
        }
        addRadioButtons(radioGroup, buttons, getDefaultValue(GROUP_INVOICE));
        mInvoiceTitle.setText(getDefaultValue(INVOICE_TITLE));
    }

    private void setPayment(JSONArray jArray) throws JSONException {
        RadioGroup radioGroup = mPaymentGroup;
        ArrayList<RadioButtonInfo> buttons = new ArrayList<RadioButtonInfo>();
        boolean useCachedValue = false;
        String cachedValue = getDefaultValue(GROUP_PAYMENT);
        String checkedValue = null;
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject item = jArray.getJSONObject(i);
            String payId = item.optString(Tags.CheckoutSubmit.PAY_ID);
            String brief = item.optString(Tags.CheckoutSubmit.BRIEF);
            if (TextUtils.equals(payId, Tags.CheckoutSubmit.PICKUP_ID_SELF)) {// 小米之家自提
                JSONArray list = item.getJSONArray(Tags.CheckoutSubmit.LIST);
                for (int j = 0; j < list.length(); j++) {
                    JSONObject it = list.getJSONObject(j);
                    RadioButtonInfo button = new RadioButtonInfo();
                    button.mTitle = String.format("%s\n%s\n%s", brief,
                            it.optString(Tags.CheckoutSubmit.NAME),
                            it.optString(Tags.CheckoutSubmit.ADDRESS));
                    String pickupId = it.optString(Tags.CheckoutSubmit.HOME_ID);
                    button.addTag(R.id.tag_checkout_pay_id, payId);
                    button.addTag(R.id.tag_checkout_pickup_id, pickupId);
                    buttons.add(button);
                    String value = String.format("%1$s_%2$s", payId, pickupId);
                    if (!useCachedValue && cachedValue != null && cachedValue.equals(value)) {
                        useCachedValue = true;
                    }
                    if (item.optBoolean(Tags.CheckoutSubmit.CHECKED)) {
                        checkedValue = value;
                    }
                }
            } else {
                RadioButtonInfo button = new RadioButtonInfo();
                button.mTitle = brief;
                button.addTag(R.id.tag_checkout_pay_id, payId);
                button.addTag(R.id.tag_checkout_pickup_id,
                        Tags.CheckoutSubmit.PICKUP_ID_DEFAULT);
                buttons.add(button);
                String value = String.format("%1$s_%2$s", payId,
                        Tags.CheckoutSubmit.PICKUP_ID_DEFAULT);
                if (!useCachedValue && cachedValue != null && cachedValue.equals(value)) {
                    useCachedValue = true;
                }
                if (item.optBoolean(Tags.CheckoutSubmit.CHECKED)) {
                    checkedValue = value;
                }
            }
        }
        if (!useCachedValue) {
            setDefaultValue(GROUP_PAYMENT, checkedValue);
        }
        addRadioButtons(radioGroup, buttons, getDefaultValue(GROUP_PAYMENT));
    }

    private void setShipment(JSONArray jArray) throws JSONException {
        RadioGroup radioGroup = mShipmentGroup;
        ArrayList<RadioButtonInfo> buttons = new ArrayList<RadioButtonInfo>();
        boolean useCachedValue = false;
        String cachedValue = getDefaultValue(GROUP_SHIPMENT);
        String checkedValue = null;
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject item = jArray.getJSONObject(i);
            String shipmentId = item.optString(Tags.CheckoutSubmit.SHIPMENT_ID);
            RadioButtonInfo button = new RadioButtonInfo();
            button.mTitle = item.optString(Tags.CheckoutSubmit.BRIEF);
            button.addTag(R.id.tag_checkout_shipment_id, shipmentId);
            buttons.add(button);
            if (!useCachedValue && cachedValue != null && cachedValue.equals(shipmentId)) {
                useCachedValue = true;
            }
            if (item.optBoolean(Tags.CheckoutSubmit.CHECKED)) {
                checkedValue = shipmentId;
            }
        }
        if (!useCachedValue) {
            setDefaultValue(GROUP_SHIPMENT, checkedValue);
        }
        addRadioButtons(radioGroup, buttons, getDefaultValue(GROUP_SHIPMENT));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton button = (RadioButton) group.findViewById(checkedId);
        if (group == mInvoiceGroup) {
            if (TextUtils
                    .equals(button.getTag().toString(), Tags.CheckoutSubmit.INVOICE_ID_COMPANY)) {
                displayInvoiceTitle(true);
                mInvoiceRadioButton = button;
                button.setBackgroundResource(R.drawable.radiobottom_middle_invoice_p);
            } else {
                if (mInvoiceRadioButton != null) {
                    mInvoiceRadioButton.setBackgroundResource(R.drawable.radiobutton_bottom_bg);
                }
                displayInvoiceTitle(false);
            }
            setDefaultValue(GROUP_INVOICE, button.getTag().toString());
        } else if (group == mPaymentGroup) {
            setDefaultValue(
                    GROUP_PAYMENT,
                    String.format("%1$s_%2$s", button.getTag(R.id.tag_checkout_pay_id),
                            button.getTag(R.id.tag_checkout_pickup_id)));
            String payId = button.getTag(R.id.tag_checkout_pay_id).toString();
            Bundle bundle = new Bundle();
            bundle.putString(Tags.CheckoutSubmit.PAY_ID, payId);
            getLoaderManager().restartLoader(REGION_PAYMENT_LOADER, bundle,
                    mRegionPaymentLoaderCallbacks);
        } else if (group == mShipmentGroup) {
            setDefaultValue(GROUP_SHIPMENT, (String) button.getTag(R.id.tag_checkout_shipment_id));
        } else if (group == mDelivertimeGroup) {
            setDefaultValue(GROUP_DELIVEERTIME, button.getTag().toString());
        }
    }

    private void displayInvoiceTitle(boolean isShow) {
        int n = isShow ? View.VISIBLE : View.GONE;
        if (isShow) {
            showInput(true);
        } else {
            showInput(false);
        }
        mInvoiceTitleBg.setVisibility(n);
    }

    private boolean getFormData() {
        if (mFormInfo == null) {
            mFormInfo = new CheckoutFormInfo();
        }
        RadioButton button = null;
        String value = null;
        int id = 0;

        // 地址
        View v = mView.findViewById(R.id.checkout_address);
        value = v.getTag().toString();
        if (TextUtils.equals(value, "0")) {
            Toast.makeText(getActivity(), R.string.checkout_form_address_empty, Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        mFormInfo.setAddressId(value);

        // 发票方式
        id = mInvoiceGroup.getCheckedRadioButtonId();
        button = (RadioButton) mInvoiceGroup.findViewById(id);
        value = button.getTag().toString();
        mFormInfo.setInvoiceType(value);
        String invoiceTitleString = null;
        if (value.equals(Tags.CheckoutSubmit.INVOICE_ID_COMPANY)) {
            String invoiceTitle = mInvoiceTitle.getText().toString();
            if (TextUtils.isEmpty(invoiceTitle)) {
                Toast.makeText(getActivity(), R.string.checkout_form_invoice_title_input_pleases,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            invoiceTitleString = mInvoiceTitle.getText().toString();
            mFormInfo.setInvoiceTitle(invoiceTitleString);
        } else
        {
            invoiceTitleString = "";
            mFormInfo.setInvoiceTitle(invoiceTitleString);
        }

        setDefaultValue(INVOICE_TITLE, invoiceTitleString);

        // 支付方式
        id = mPaymentGroup.getCheckedRadioButtonId();
        button = (RadioButton) mPaymentGroup.findViewById(id);
        String payId = button.getTag(R.id.tag_checkout_pay_id).toString();
        String pickupId = button.getTag(R.id.tag_checkout_pickup_id).toString();
        mFormInfo.setPayId(payId);
        mFormInfo.setPickupId(pickupId);

        // 配送方式
        id = mShipmentGroup.getCheckedRadioButtonId();
        button = (RadioButton) mShipmentGroup.findViewById(id);
        Object shipmentId = button.getTag(R.id.tag_checkout_shipment_id);
        if (shipmentId != null) {
            mFormInfo.setShipmentId(shipmentId.toString());
        }

        // 送货时间
        id = mDelivertimeGroup.getCheckedRadioButtonId();
        button = (RadioButton) mDelivertimeGroup.findViewById(id);
        mFormInfo.setBestTimeId(button.getTag().toString());

        /** mi-home shopping info */
        mFormInfo.setMihomeBuyConsignee(mMihomeBuyConsignee.getText().toString());
        mFormInfo.setMihomeBuyTel(mMihomeBuyTel.getText().toString());
        mFormInfo.setMihomeBuyId(mMihomeBuyId);
        return true;
    }

    private void setFormData(JSONObject json) {
        JSONObject address;
        try {
            JSONObject dataJson = json.getJSONObject(Tags.DATA);
            address = dataJson.optJSONObject(Tags.AddressInfo.ADDRESS);
            if (address != null) {
                if (address != null && address.length() != 0) {
                    mAddressId = address.optString(Tags.AddressInfo.ID);
                    mAddress.setTag(mAddressId);
                    String tel = address.optString(Tags.AddressInfo.TEL);
                    mAddressBottom.setText(getResources().getString(R.string.address_title,
                            address.optString(Tags.AddressInfo.CONSIGNEE),
                            PhoneFormat.valueOf(tel)));
                    mMihomeBuyConsignee.setText(address.optString(Tags.AddressInfo.CONSIGNEE));
                    mMihomeBuyTel.setText(address.optString(Tags.AddressInfo.TEL));
                    mAddressUp.setText(getResources().getString(R.string.address_area,
                            address.optJSONObject(Tags.AddressInfo.PROVINCE)
                                    .optString("name"),
                            address.optJSONObject(Tags.AddressInfo.CITY).optString("name"),
                            address.optJSONObject(Tags.AddressInfo.DISTRICT).optString("name")));
                    mRegionId = address.optJSONObject(Tags.AddressInfo.DISTRICT).optString("id");
                    mAddressMiddle.setText(getResources().getString(R.string.address_location,
                            address.optString(Tags.AddressInfo.ADDRESS),
                            address.optString(Tags.AddressInfo.ZIPCODE)));
                }
                mCheckoutAddressEmpty.setVisibility(View.GONE);
                mAddress.setVisibility(View.VISIBLE);
            } else {
                mAddress.setTag("0");
                mCheckoutAddressEmpty.setVisibility(View.VISIBLE);
                mAddress.setVisibility(View.GONE);
            }
            /** mi-home shopping */
            if (mIsMihomeShopping) {
                JSONObject mihomeBuyInfo = dataJson.optJSONObject(Tags.MihomeBuyInfo.SELF);
                mMihomeBuyAddr.setText(mihomeBuyInfo.optString(Tags.MihomeBuyInfo.NAME));
                mCheckoutAddressEmpty.setVisibility(View.GONE);
                mAddress.setVisibility(View.GONE);
            }
            mIsPersonalInvoice = dataJson.optBoolean(Tags.CheckoutSubmit.INVOICE_OPEN, false);
            for (int i = 0; i < mRowTitles.length; i++) {
                String rowTitle = mRowTitles[i];
                JSONArray jArray;
                jArray = dataJson.optJSONArray(rowTitle);
                if (jArray == null || jArray.length() == 0) {
                    break;
                }
                setFormRow(rowTitle, jArray);
            }
            mInvoiceTitle.setText(getDefaultValue(INVOICE_TITLE));

            /**
             * 在下单的时候有个提示，告知用户订单的一些状况，例如过年时候发货可能会慢一点啊，等等。
             */
            String hint = dataJson.optString("tip");
            if (TextUtils.isEmpty(hint)) {
                mCheckoutHintContainer.setVisibility(View.GONE);
            } else {
                mCheckoutHintContainer.setVisibility(View.VISIBLE);
                mCheckoutHint.setText(hint);
            }
        } catch (JSONException e) {
            Toast.makeText(getActivity(), getString(R.string.checkout_set_form_data_exception),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void removeFormData() {
        mAddress.setTag("");
        mAddressUp.setText("");
        mAddressMiddle.setText("");
        mAddressBottom.setText("");
        mPaymentGroup.removeAllViews();
        mShipmentGroup.removeAllViews();
        mInvoiceGroup.removeAllViews();
        mDelivertimeGroup.removeAllViews();
    }

    private void onNext() {
        if (!getFormData()) {
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(Tags.OrderSubmit.ADDRESS_ID, mFormInfo.getAddressId());
            json.put(Tags.OrderSubmit.PAY_ID, mFormInfo.getPayId());
            json.put(Tags.OrderSubmit.PICKUP_ID, mFormInfo.getPickupId());
            json.put(Tags.OrderSubmit.SHIPMENT_ID, mFormInfo.getShipmentId());
            json.put(Tags.OrderSubmit.BEST_TIME, mFormInfo.getBestTimeId());
            json.put(Tags.OrderSubmit.INVOICE_TYPE, mFormInfo.getInvoiceType());
            json.put(Tags.OrderSubmit.INVOICE_TITLE, mFormInfo.getInvoiceTitle());
            json.put(Tags.OrderSubmit.COUPON_TYPE, mFormInfo.getCouponType());
            json.put(Tags.OrderSubmit.COUPON_CODE, mFormInfo.getCouponCode());
            json.put(Tags.OrderSubmit.MIHOME_BUY_ID, mFormInfo.getMihomeBuyId());
            JSONObject extendField = new JSONObject();
            extendField.put(Tags.OrderSubmit.EXTEND_FIELD_CONSIGNESS,
                    mFormInfo.getMihomeBuyConsignee());
            extendField.put(Tags.OrderSubmit.EXTEND_FIELD_TEL, mFormInfo.getMihomeBuyTel());
            json.put(Tags.OrderSubmit.EXTEND_FIELD, extendField);
            ShoppingActivity activity = (ShoppingActivity) getActivity();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON,
                    json.toString());
            Fragment fragment = ((BaseActivity) getActivity())
                    .getFragmentByTag(ShoppingActivity.Fragments.TAG_ORDER_SUBMIT_FRAGMENT);
            if (fragment != null) {
                ((OrderSubmitFragment) fragment).setData(json.toString());
            }
            activity.showFragment(ShoppingActivity.Fragments.TAG_ORDER_SUBMIT_FRAGMENT, bundle,
                    true);
        } catch (JSONException e) {
            Toast.makeText(getActivity(),
                    getString(R.string.checkout_build_request_params_exception), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.RequestCode.CODE_ADDRESS) {
            String selectedAddressId = data
                    .getStringExtra(Constants.Intent.EXTRA_ADDRESS_ID);
            LogUtil.d(TAG, "finish from address activity:" + selectedAddressId);

            if (mLoader == null) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString(Tags.CheckoutSubmit.ADDRESS_ID, selectedAddressId);
            mContainer.setVisibility(View.GONE);
            getLoaderManager().restartLoader(CHECKOUT_LOADER, bundle, this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.title_checkout);
    }

    private String getDefaultValue(int key) {
        String ret = mFormDefaultValue.get(key);
        LogUtil.d(TAG, "getDefaultValue: key is " + key + ", value is " + ret);
        return ret;
    }

    private void setDefaultValue(int key, String value) {
        LogUtil.d(TAG, "SetDefaultValue: key is " + key + ", value is " + value);
        mFormDefaultValue.put(key, value);
    }

    private void addRadioButtons(RadioGroup group, ArrayList<RadioButtonInfo> items,
            String defaultValue) {
        LogUtil.d(TAG, "addRadioButtons");
        String value = null;
        RadioButton first = null;
        int count = items.size();
        for (int i = 0; i < count; i++) {
            RadioButtonInfo item = items.get(i);
            RadioButton button = new RadioButton(getActivity());
            if (i == 0) {
                first = button;
            }
            button.setText(item.mTitle);
            if (item.mTags.size() == 0) {
                button.setTag(item.mTag);
            } else {
                for (int j = 0; j < item.mTags.size(); j++) {
                    Tag tag = item.mTags.get(j);
                    button.setTag(tag.key, tag.value);
                }
            }
            group.addView(button);

            if (count == 1) {
                button.setBackgroundResource(R.drawable.radiobutton_single_bg);
            } else if (i == 0) {
                button.setBackgroundResource(R.drawable.radiobutton_up_bg);
            } else if (i == count - 1) {
                button.setBackgroundResource(R.drawable.radiobutton_bottom_bg);
            } else {
                button.setBackgroundResource(R.drawable.radiobutton_middle_bg);
            }
            button.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            button.setLayoutParams(params);
            if (group == mPaymentGroup) {
                value = String.format("%1$s_%2$s", button.getTag(R.id.tag_checkout_pay_id),
                        button.getTag(R.id.tag_checkout_pickup_id));
            } else if (group == mShipmentGroup) {
                value = button.getTag(R.id.tag_checkout_shipment_id).toString();
            } else {
                value = button.getTag().toString();
            }
            if (defaultValue == null) {
                first.setChecked(true);
            } else if (TextUtils.equals(value, defaultValue)) {
                button.setChecked(true);
            }
        }
    }

    private void openAddressActivity(String addressId) {
        Intent intent = new Intent(getActivity(), AddressActivity.class);
        intent.setAction(Constants.Intent.ACTION_USE_ADDRESS);
        intent.putExtra(Constants.Intent.EXTRA_ADDRESS_ID, addressId);
        startActivityForResult(intent, Constants.RequestCode.CODE_ADDRESS);
    }

    public ShoppingActivity getParent() {
        return (ShoppingActivity) getActivity();
    }

    class RadioButtonInfo {
        public String mTitle;
        public String mTag;
        public ArrayList<Tag> mTags = new ArrayList<Tag>();

        public void addTag(int key, String value) {
            Tag tag = new Tag();
            tag.key = key;
            tag.value = value;
            mTags.add(tag);
        }

        class Tag {
            public int key;
            public String value;
        }
    }

    public void showInput(boolean isShow) {
        if (isShow) {
            mInvoiceTitle.setFocusable(true);
            mInvoiceTitle.setFocusableInTouchMode(true);
            mInvoiceTitle.requestFocus();
            Utils.SoftInput.show(mInvoiceTitle.getContext(), mInvoiceTitle);
        } else {
            mInvoiceTitle.setFocusable(false);
            mInvoiceTitle.setFocusableInTouchMode(false);
            Utils.SoftInput.hide(mInvoiceTitle.getContext(), mInvoiceTitle.getWindowToken());
        }
    }
}
