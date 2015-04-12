
package com.xiaomi.xms.sales.ui;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.OrderEditActivity;
import com.xiaomi.xms.sales.adapter.EmailSpinnerAdapter;
import com.xiaomi.xms.sales.loader.OrderInfoLoader;
import com.xiaomi.xms.sales.loader.OrderUserInfoLoader;
import com.xiaomi.xms.sales.loader.RequestLoader;
import com.xiaomi.xms.sales.loader.RequestLoader.Result;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.ui.OrderEditDetailFragment.RadioButtonInfo.Tag;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.PrinterService;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class OrderEditDetailFragment extends BaseFragment implements
        LoaderCallbacks<OrderUserInfoLoader.Result>, OnCheckedChangeListener {
    private static final String TAG = "OrderEditDetailFragment";
    private final static int ORDER_USERINFO_LOADER = 1;

    private static final int GET_ORDER_INFO = 1001;
    // LoaderManager用来区别不同的Loader
    private static final int REQUEST_LOADER = 0;
    // 用于区分不同的请求
    private static final int REQUEST_CODE = 0;
    private EditText mUserNameText;
    private EditText mUserTelText;
    private EditText mUserEmailText;
    private Spinner mUserEmailSpinner;
    private EditText mUserEmailDomainText;
    private RadioGroup mInvoiceRadioGroup;
    private EditText mInvoiceTitleText;
    private Button mSubmitBtn;
    private ProgressDialog mProgressDialog;
    private EmptyLoadingView mLoadingView;
    private ViewGroup mInvoiceTitleBg;
    private String mUserNameStr;
    private String mUserTelStr;
    private String mUserEmailStr;
    private String mInvoiceTitleStr;
    private String mUserEmailDomainStr;
    private RequestLoader mRequestLoader;
    private Handler mHandler = new Handler();
    private String mOrderId;
    private RadioButton mInvoiceRadioButton;
    private final static int GROUP_INVOICE = 1;
    private EmailSpinnerAdapter mEmailSpinnerAdapter;
    private ArrayList<String> mEmailList;
    private HashMap<Integer, String> mFormDefaultValue = new HashMap<Integer, String>();
    private String printerIP;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_edit_detail_fragment, container, false);
        mUserNameText = (EditText) view.findViewById(R.id.order_edit_user_name);
        mUserTelText = (EditText) view.findViewById(R.id.order_edit_user_tel);
        mUserEmailText = (EditText) view.findViewById(R.id.order_edit_user_email);
        mInvoiceTitleText = (EditText) view.findViewById(R.id.order_edit_form_invoice_title);
        mUserEmailSpinner = (Spinner) view.findViewById(R.id.order_edit_user_email_spinner);
        mInvoiceRadioGroup = (RadioGroup) view.findViewById(R.id.order_edit_form_radiogroup_invoice);
        mSubmitBtn = (Button) view.findViewById(R.id.order_edit_submit);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mInvoiceTitleBg = (ViewGroup) view.findViewById(R.id.corder_edit_form_invoice_title_bg);
        mUserEmailDomainText = (EditText) view.findViewById(R.id.order_edit_user_email_domain);
        mEmailSpinnerAdapter = new EmailSpinnerAdapter(getActivity());
        mUserEmailSpinner.setAdapter(mEmailSpinnerAdapter);
        mInvoiceRadioGroup.setOnCheckedChangeListener(this);
        handleIntent();
        return view;
    }

    private void handleIntent() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String action = bundle.getString(Constants.Intent.EXTRA_ORDER_EDIT_ACTION);
            mOrderId = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            printerIP = bundle.getString(Constants.Intent.EXTRA_PRINTER_IP);
            if (TextUtils.equals(action, "ADD")) {
                setInvoice();
                String[] email = getResources().getStringArray(R.array.email);
                mEmailList = new ArrayList<String>(Arrays.asList(email));
                if (mEmailList != null && mEmailList.size() > 0) {
                    mEmailSpinnerAdapter.updateData(mEmailList);
                }
                getActivity().setTitle(R.string.order_add_title);
                mSubmitBtn.setText(R.string.order_edit_submit);
            } else if (TextUtils.equals(action, "EDIT")) {
                getActivity().setTitle(R.string.order_edit_title);
                mSubmitBtn.setText(R.string.order_edit_confirm);
                getLoaderManager().initLoader(ORDER_USERINFO_LOADER, null, this);
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSubmitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput()) {
                    showDialog("");
                    // 这里要得到loadManager
                    getLoaderManager().initLoader(REQUEST_LOADER, null, mRequestCallback);
                    Request request = new Request(HostManager.URL_XMS_SALE_API);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("serviceNumber", mOrderId);
                        jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                        jsonObject.put("consignee", mUserNameStr);
                        jsonObject.put("tel", mUserTelStr);
                        String email = "";
                        if (!TextUtils.isEmpty(mUserEmailDomainStr)) {
                            email = mUserEmailStr + "@" + mUserEmailDomainStr;
                        } else if (!TextUtils.isEmpty(mUserEmailStr)) {
                            email = mUserEmailStr + "@"
                                    + mUserEmailSpinner.getSelectedView().getTag().toString();
                        }
                        jsonObject.put("email", email);
                        jsonObject.put("invoiceType", getDefaultValue(GROUP_INVOICE));
                        if (TextUtils.equals(getDefaultValue(GROUP_INVOICE), Tags.CheckoutSubmit.INVOICE_ID_COMPANY)) {
                            jsonObject.put("invoiceTitle", mInvoiceTitleStr);
                        }
                        String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_UPDATECONSIGNEEINF,
                                jsonObject);
                        if (!TextUtils.isEmpty(data)) {
                            request.addParam(Tags.RequestKey.DATA, data);
                            mRequestLoader.load(REQUEST_CODE, request);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mUserEmailSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (TextUtils.equals(view.getTag().toString(), "其它")) {
                    mUserEmailDomainText.setVisibility(View.VISIBLE);
                    mUserEmailSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                LogUtil.i(TAG, "onNothingSelected");
            }
        });

    }

    private LoaderCallbacks<RequestLoader.Result> mRequestCallback = new LoaderCallbacks<RequestLoader.Result>() {

        @Override
        public Loader<Result> onCreateLoader(int id, Bundle bundle) {
            mRequestLoader = new RequestLoader(getActivity());
            return mRequestLoader;
        }

        @Override
        public void onLoadFinished(Loader<Result> loader, Result data) {
            if (data != null) {
                if (REQUEST_CODE == data.mRequestCode) {
                    dismissDialog();
                    // 每次调用结束必须销毁loader
                    getLoaderManager().destroyLoader(REQUEST_LOADER);
                    if (data.mStatus == Request.STATUS_OK) {
                        JSONObject jsonObj = data.mData;
                        if (jsonObj != null) {
                            LogUtil.d(TAG, jsonObj.toString());
                            if (Tags.isJSONReturnedOK(jsonObj)) {
                            	ToastUtil.show(getActivity(), R.string.order_edit_ok);
                            	if(printerIP != null && printerIP.length() > 0){
                            		getLoaderManager().restartLoader(GET_ORDER_INFO, null, new LoaderCallbacks<OrderInfoLoader.Result>() {
                              	        @Override
                              	        public OrderInfoLoader onCreateLoader(int id, Bundle arg1) {
                              	            if (id == GET_ORDER_INFO) {
                              	            	OrderInfoLoader mLoader = new OrderInfoLoader(getActivity());
                              	                mLoader.setNeedSecurityKeyTask(false);
                              	                mLoader.setNeedDatabase(false);
                              	                mLoader.setOrderId(mOrderId);
                              	                mLoader.setProgressNotifiable(mLoadingView);
                              	                return mLoader;
                              	            }
                              	            return null;
                              	        }

                              	        @Override
                              	        public void onLoadFinished(Loader<OrderInfoLoader.Result> loader,
                              	                OrderInfoLoader.Result data) {
                              	            if (data != null && data.mOrderInfo != null) {
                              	            	final String PRINT_IP = printerIP;
                              	            	final Order order = data.mOrderInfo;
                          	            		new Thread(new Runnable() {

    													@Override
    													public void run() {
    														PrinterService printerService;
    														try {
    															printerService = new PrinterService(PRINT_IP);
    															printerService.print(order,getActivity());
    														} catch (IOException e) {
    															e.printStackTrace();
    														}
    														
    													}
                          	            			
                          	            		}).start();
                          	            		
                              	            }
                              	          mHandler.post(new Runnable() {
                                              @Override
                                              public void run() {
                                                  ((OrderEditActivity) getActivity()).onBackPressed(true);
                                              }
                                          });
                              	        }

                              	        @Override
                              	        public void onLoaderReset(Loader<OrderInfoLoader.Result> loader) {
                              	        }
                                      });
                            	}
                            	else{
                            		mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((OrderEditActivity) getActivity()).onBackPressed(true);
                                        }
                                    });
                            	}
                                
                            } else {
                                ToastUtil.show(getActivity(), R.string.order_edit_error);
                            }
                        }
                    } else {
                        ToastUtil.show(getActivity(), R.string.order_edit_error);
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Result> arg0) {

        }
    };

    private boolean checkInput() {
        mUserNameStr = mUserNameText.getText().toString();
        mUserTelStr = mUserTelText.getText().toString();
        if (!TextUtils.isEmpty(mUserTelStr)) {
            if (!isPhoneValid(mUserTelStr) || mUserTelStr.length() != 11) {
                ToastUtil.show(getActivity(), R.string.order_edit_tel_error_info);
                mUserTelText.requestFocus();
                return false;
            }
        }
        mUserEmailStr = mUserEmailText.getText().toString();
        String invoice = getDefaultValue(GROUP_INVOICE);
        if (TextUtils.isEmpty(invoice) || TextUtils.equals(invoice, "0")) {
            ToastUtil.show(getActivity(), R.string.order_invoice_empty_info);
            return false;
        }
        if (invoice == Tags.CheckoutSubmit.INVOICE_ID_COMPANY) {
            mInvoiceTitleStr = mInvoiceTitleText.getText().toString();
            if (TextUtils.isEmpty(mInvoiceTitleStr)) {
                ToastUtil.show(getActivity(), R.string.order_edit_invoice_empty_info);
                mInvoiceTitleText.requestFocus();
                return false;
            }
        }
        if (TextUtils.equals(mUserEmailSpinner.getSelectedView().getTag().toString(), "其它")) {
            mUserEmailDomainStr = mUserEmailDomainText.getText().toString();
            if (TextUtils.isEmpty(mUserEmailDomainStr)) {
                ToastUtil.show(getActivity(), R.string.order_edit_email_domain_empty_info);
                mUserEmailDomainText.requestFocus();
                return false;
            }
        }
        if (!TextUtils.isEmpty(mUserEmailStr)) {
            String emailAll = "";
            if (!TextUtils.isEmpty(mUserEmailDomainStr)) {
                emailAll = mUserEmailStr
                        + "@" + mUserEmailDomainStr;
            } else {
                emailAll = mUserEmailStr + "@"
                        + mUserEmailSpinner.getSelectedView().getTag().toString();
            }
            if (!isEmailValid(emailAll)) {
                ToastUtil.show(getActivity(),
                        R.string.order_edit_email_error_info);
                mUserEmailText.requestFocus();
                return false;
            }
        }

        return true;
    }

    private boolean isPhoneValid(CharSequence phone) {
        return Patterns.PHONE.matcher(phone).matches();
    }

    private boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
        }
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.progressbar);
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton button = (RadioButton) group.findViewById(checkedId);
        if (group == mInvoiceRadioGroup) {
            if (TextUtils
                    .equals(button.getTag().toString(), Tags.CheckoutSubmit.INVOICE_ID_COMPANY)) {
                displayInvoiceTitle(View.VISIBLE, true);
                mInvoiceRadioButton = button;
                button.setBackgroundResource(R.drawable.radiobottom_middle_invoice_p);
            } else {
                if (mInvoiceRadioButton != null) {
                    mInvoiceRadioButton.setBackgroundResource(R.drawable.radiobutton_bottom_bg);
                }
                displayInvoiceTitle(View.GONE, false);
            }
            setDefaultValue(GROUP_INVOICE, button.getTag().toString());
        }

    }

    private void setInvoice() {
        ArrayList<RadioButtonInfo> buttons = new ArrayList<RadioButtonInfo>();

        RadioButtonInfo button2 = new RadioButtonInfo();
        button2.mTitle = getString(R.string.checkout_personal_invoice);
        button2.mTag = Tags.CheckoutSubmit.INVOICE_ID_PERSONAL;
        buttons.add(button2);

        RadioButtonInfo button3 = new RadioButtonInfo();
        button3.mTitle = getString(R.string.checkout_company_invoice);
        button3.mTag = Tags.CheckoutSubmit.INVOICE_ID_COMPANY;
        buttons.add(button3);

        addRadioButtons(mInvoiceRadioGroup, buttons, getDefaultValue(GROUP_INVOICE));
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

    private void displayInvoiceTitle(int visibility, boolean isShow) {
        showInput(isShow);
        mInvoiceTitleBg.setVisibility(visibility);
    }

    public void showInput(boolean isShow) {
        if (isShow) {
            mInvoiceTitleText.setFocusable(true);
            mInvoiceTitleText.setFocusableInTouchMode(true);
            mInvoiceTitleText.requestFocus();
            Utils.SoftInput.show(mInvoiceTitleText.getContext(), mInvoiceTitleText);
        } else {
            mInvoiceTitleText.setFocusable(false);
            mInvoiceTitleText.setFocusableInTouchMode(false);
            Utils.SoftInput.hide(mInvoiceTitleText.getContext(), mInvoiceTitleText.getWindowToken());
        }
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
            value = button.getTag().toString();
            if (defaultValue == null) {
                first.setChecked(true);
            } else if (TextUtils.equals(value, defaultValue)) {
                button.setChecked(true);
            }
        }
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

    @Override
    public Loader<OrderUserInfoLoader.Result> onCreateLoader(int id, Bundle bundle) {
        if (id == ORDER_USERINFO_LOADER) {
            mLoader = new OrderUserInfoLoader(getActivity(), mOrderId);
            mLoader.setProgressNotifiable(mLoadingView);
            mLoader.setNeedDatabase(false);
            return (Loader<OrderUserInfoLoader.Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<OrderUserInfoLoader.Result> arg0, OrderUserInfoLoader.Result data) {
        mInvoiceRadioGroup.removeAllViews();
        String[] email = getResources().getStringArray(R.array.email);
        mEmailList = new ArrayList<String>(Arrays.asList(email));
        if (mEmailList != null && mEmailList.size() > 0) {
            mEmailSpinnerAdapter.updateData(mEmailList);
        }
        String userName = data.mOrderUserInfo.getUserName();
        mUserNameText.setText(!TextUtils.isEmpty(userName) && !"null".equals(userName) ? userName : "");
        String userTel = data.mOrderUserInfo.getUserTel();
        mUserTelText.setText(!TextUtils.isEmpty(userTel) && !"null".equals(userTel) ? userTel : "");
        String emailStr = data.mOrderUserInfo.getUserEmail();
        if (!TextUtils.isEmpty(emailStr) && !"null".equals(emailStr) && emailStr.contains("@")) {
            mUserEmailText.setText(emailStr.split("@")[0]);
            mUserEmailSpinner.setSelection(-1 == mEmailList.indexOf(emailStr.split("@")[1]) ? mEmailList
                    .size() - 1 : mEmailList.indexOf(emailStr.split("@")[1]));
            mUserEmailDomainText.setText(emailStr.split("@")[1]);
        }
        String invoiceType = data.mOrderUserInfo.getInvoiceType();
        if (TextUtils.equals("0", invoiceType)) {
            invoiceType = Tags.CheckoutSubmit.INVOICE_ID_PERSONAL;
        }
        setDefaultValue(GROUP_INVOICE, invoiceType);
        setInvoice();
        View button = mInvoiceRadioGroup.getChildAt(Integer.parseInt(invoiceType) - 1);
        if (TextUtils.equals(invoiceType, Tags.CheckoutSubmit.INVOICE_ID_COMPANY)) {
            displayInvoiceTitle(View.VISIBLE, false);
            mInvoiceTitleText.setText(data.mOrderUserInfo.getInvoiceTitle());
            mInvoiceTitleText.setSelection(data.mOrderUserInfo.getInvoiceTitle().length());
            button.setBackgroundResource(R.drawable.radiobottom_middle_invoice_p);
        } else {
            if (mInvoiceRadioButton != null) {
                mInvoiceRadioButton.setBackgroundResource(R.drawable.radiobutton_bottom_bg);
            }
            displayInvoiceTitle(View.GONE, false);
        }
        mUserNameText.setSelection(!TextUtils.isEmpty(userName) && !"null".equals(userName) ? userName.length() : 0);
        mUserNameText.requestFocus();
    }

    @Override
    public void onLoaderReset(Loader<OrderUserInfoLoader.Result> arg0) {

    }

    // 显示退出的Dialog
    public void showExitDialog() {
        final BaseAlertDialog dialog = new BaseAlertDialog(getActivity());
        dialog.setTitle(R.string.order_user_info_exit_dialog_title);
        dialog.setMessage(R.string.order_user_info_exit_dialog_message);
        dialog.setPositiveButton(R.string.order_user_info_exit_dialog_ask_ok, new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
