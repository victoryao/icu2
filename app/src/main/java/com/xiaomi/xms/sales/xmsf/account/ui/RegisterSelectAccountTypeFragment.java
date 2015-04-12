
package com.xiaomi.xms.sales.xmsf.account.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.xmsf.account.exception.InvalidResponseException;
import com.xiaomi.xms.sales.xmsf.account.utils.CloudHelper;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class RegisterSelectAccountTypeFragment extends StepsFragment {
    private static final String TAG = "RegisterSelectAccountTypeFragment";

    private static final int ERROR_PHONE_USED = 1;
    private static final int ERROR_EMAIL_USED = 2;
    private static final int ERROR_NETWORK = 4;
    private static final int ERROR_SERVER = 5;
    private static final int ERROR_NETWORK_GW = 14;
    private static final int ERROR_SERVER_GW = 15;
    private static final int ERROR_NETWORK_TIMEOUT_GW = 16;
    private static final int ERROR_NO_SIM = 17;

    private RadioGroup mAccountTypeRadios;
    private View mEmailPanel;
    private EditText mEmailText;
    private RadioButton mEmailRegioButton;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // view's visibility should be set here
        setButtonPreviousGone(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.micloud_register_select_type, container, false);

        mAccountTypeRadios = (RadioGroup) v.findViewById(R.id.account_types);
        mEmailPanel = v.findViewById(R.id.email_input_panel);
        mEmailText = (EditText) v.findViewById(R.id.et_email);
        mEmailText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEmailText.setError(null);
            }
        });

        String[] accountTypes = getResources().getStringArray(R.array.new_account_types);
        for (int i = 0; i < accountTypes.length; i++) {
            LogUtil.d(TAG, "get account reg type:" + accountTypes[i]);
            RadioButton typeView = new RadioButton(getActivity());
            typeView.setId(i + 1); // the id should be positive
            typeView.setText(accountTypes[i]);
            typeView.setTextColor(getResources()
                    .getColorStateList(R.color.primary_text));
            mAccountTypeRadios
                    .addView(typeView, i, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            if (accountTypes.length == 1) {
                typeView.setChecked(true);
                typeView.setBackgroundResource(R.drawable.radiobutton_single_bg);
            } else if (i == 0) {
                typeView.setChecked(true);
                typeView.setBackgroundResource(R.drawable.radiobutton_up_bg);
            } else if (i == accountTypes.length - 1) {
                typeView.setBackgroundResource(R.drawable.radiobutton_bottom_bg);
            } else {
                typeView.setBackgroundResource(R.drawable.radiobutton_middle_bg);
            }
            typeView.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // check the first radio
        mAccountTypeRadios.check(1);
        mAccountTypeRadios.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup,
                            int checkedId) {
                        switch (checkedId) {
                            case 1:
                                if (mEmailRegioButton != null) {
                                    mEmailRegioButton
                                            .setBackgroundResource(R.drawable.radiobutton_bottom_bg);
                                }
                                mEmailPanel.setVisibility(View.GONE);
                                displaySoftInputIfNeed(getView(), false);
                                break;
                            case 2:
                                mEmailRegioButton = (RadioButton) radioGroup
                                        .findViewById(checkedId);
                                mEmailRegioButton
                                        .setBackgroundResource(R.drawable.radiobottom_middle_noline_p);
                                mEmailPanel.setVisibility(View.VISIBLE);
                                mEmailText.requestFocus();
                                displaySoftInputIfNeed(mEmailText, true);
                                break;
                        }
                    }
                });
        mEmailText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    triggerNextStep();
                    return true;
                }
                return false;
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        displaySoftInputIfNeed(mEmailText, getSelectedAccountTypeIndex() == 1);
    }

    public int getSelectedAccountTypeIndex() {
        // the button id was added by 1
        return mAccountTypeRadios.getCheckedRadioButtonId() - 1;
    }

    private class CheckEmailTask extends AsyncTask<Void, Void, Integer> {
        private ProgressDialog mProgressDialog;
        private Runnable mSuccessRunnable;
        private String mEmail;

        private CheckEmailTask(String email, Runnable runnable) {
            mSuccessRunnable = runnable;
            mEmail = email;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result == Activity.RESULT_OK) {
                mSuccessRunnable.run();
            } else {
                handleCheckFailed(result);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            LogUtil.d(TAG, "get email from server:" + mEmail);
            try {
                String userId = CloudHelper.getUserIdForEmail(mEmail);
                if (!TextUtils.isEmpty(userId)) {
                    LogUtil.d(TAG, "email registered, userId:" + userId);
                    return ERROR_EMAIL_USED;
                }
            } catch (IOException e) {
                Log.e(TAG, "failed to check email due to IO error");
                return ERROR_NETWORK;
            } catch (InvalidResponseException e) {
                Log.e(TAG, "failed to check email due to server error");
                return ERROR_SERVER;
            }
            return Activity.RESULT_OK;
        }
    }

    private class CheckPhoneTask extends AsyncTask<Void, Void, Integer> {
        private ProgressDialog mProgressDialog;
        private Runnable mSuccessRunnable;

        public CheckPhoneTask(Runnable runnable) {
            mSuccessRunnable = runnable;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result == Activity.RESULT_OK) {
                mSuccessRunnable.run();
            } else {
                handleCheckFailed(result);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(
                    Activity.TELEPHONY_SERVICE);
            final String deviceId = tm.getDeviceId();
            final String imsi = tm.getSubscriberId();

            // check the phone if is previous registered
            String queryPhone;
            try {
                queryPhone = CloudHelper.queryPhone(deviceId, imsi);
            } catch (IOException e) {
                Log.e(TAG, "failed to check device due to IO error");
                return ERROR_NETWORK;
            } catch (InvalidResponseException e) {
                Log.e(TAG, "failed to check device due to server error");
                return ERROR_SERVER;
            }
            LogUtil.d(TAG, "get phone from server:" + queryPhone);
            if (!TextUtils.isEmpty(queryPhone)) {
                String userId = null;
                try {
                    userId = CloudHelper.getUserIdForPhone(queryPhone);
                } catch (IOException e) {
                    Log.e(TAG, "failed to check phone due to IO error");
                    return ERROR_NETWORK;
                } catch (InvalidResponseException e) {
                    Log.e(TAG, "failed to check device due to server error");
                    return ERROR_SERVER;
                }
                if (!TextUtils.isEmpty(userId)) {
                    LogUtil.d(TAG, "phone registered, userId:" + userId);
                    return ERROR_PHONE_USED;
                }
            }
            return Activity.RESULT_OK;
        }
    }

    /**
     * Query sms gateway records from server.
     */
    private class QuerySmsGwTask extends AsyncTask<Void, Void, Integer> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            String smsGw = null;
            try {
                Log.d(TAG, "Start fetching sms gateways");
                smsGw = CloudHelper.selectSmsGwByServer(getActivity());
            } catch (SocketTimeoutException e) {
                return ERROR_NETWORK_GW;
            } catch (IOException e) {
                Log.e(TAG, "failed to fetching sms gateways to IO error");
                return ERROR_NETWORK_GW;
            } catch (InvalidResponseException e) {
                Log.e(TAG, "failed to fetching sms gateways to server error");
                return ERROR_SERVER_GW;
            }
            if (!TextUtils.isEmpty(smsGw)) {
                Log.d(TAG, "Fetched sms gateways, writing into system settings.");
                Utils.Preference.setStringPref(getActivity(), Constants.Prefence.PREF_KEY_SMS_WG,
                        smsGw);
            } else {
                return ERROR_SERVER_GW;
            }
            return Activity.RESULT_OK;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result == Activity.RESULT_OK) {
                // reg via phone number
                new CheckPhoneTask(new Runnable() {

                    @Override
                    public void run() {
                        final RegisterPasswordFragment passwordFragment = new RegisterPasswordFragment();
                        passwordFragment.setNewAccountType(Constants.Account.REG_TYPE_PHONE_NUMBER);
                        replaceToFragment(passwordFragment, false);
                    }
                }).execute();
            } else {
                handleCheckFailed(result);
            }
        }
    }

    private void handleCheckFailed(int error) {
        String reason;
        switch (error) {
            case ERROR_PHONE_USED: {
                reason = getString(R.string.error_dup_phone);
                break;
            }
            case ERROR_EMAIL_USED: {
                reason = getString(R.string.error_dup_email);
                break;
            }
            case ERROR_NETWORK: {
                reason = getString(R.string.error_network);
                break;
            }
            case ERROR_SERVER: {
                reason = getString(R.string.error_server);
                break;
            }
            case ERROR_NETWORK_GW: {
                reason = getString(R.string.error_network);
                break;
            }
            case ERROR_SERVER_GW: {
                reason = getString(R.string.error_server);
                break;
            }
            case ERROR_NETWORK_TIMEOUT_GW: {
                reason = getString(R.string.error_timeout);
                break;
            }
            case ERROR_NO_SIM: {
                reason = getString(R.string.error_no_sim);
                break;
            }
            default: {
                reason = getString(R.string.error_unknown);
                break;
            }
        }
        BaseAlertDialog dialog = new BaseAlertDialog(getActivity());
        dialog.setMessage(reason);
        // builder.setIconAttribute(android.R.attr.alertDialogIcon);
        dialog.setPositiveButton(android.R.string.cancel, null);
        dialog.show();
    }

    private SpannableStringBuilder getErrorSpanString(int resId) {
        String err = getActivity().getString(resId);
        TextAppearanceSpan highlightTextAppearanceSpan = new TextAppearanceSpan(
                getActivity(), R.style.TextAppearance_Notice_Normal);
        SpannableStringBuilder builder = new SpannableStringBuilder(err);
        builder.setSpan(highlightTextAppearanceSpan, builder.length()
                - err.length(),
                builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    @Override
    protected void onButtonNextClicked() {
        final int index = getSelectedAccountTypeIndex();
        switch (index) {
            case 0: {
                TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(
                        Context.TELEPHONY_SERVICE);
                String mccmnc = tm.getNetworkOperator();
                boolean nosim = tm.getSimState() == 1 ? true : false;
                if (TextUtils.isEmpty(mccmnc) || nosim) {
                    handleCheckFailed(ERROR_NO_SIM);
                    return;
                }
                QuerySmsGwTask task = new QuerySmsGwTask();
                task.execute();

                break;
            }
            case 1: {
                // reg via email
                final String email = mEmailText.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmailText.setError(getErrorSpanString(R.string.micloud_error_email));
                    return;
                }
                new CheckEmailTask(email, new Runnable() {

                    @Override
                    public void run() {
                        final RegisterPasswordFragment passwordFragment = new RegisterPasswordFragment();
                        passwordFragment.setNewAccountType(Constants.Account.REG_TYPE_EMAIL);

                        Bundle data = new Bundle();
                        data.putString("email", email);
                        passwordFragment.setArguments(data);
                        replaceToFragment(passwordFragment, false);
                    }
                }).execute();
                break;
            }
        }
    }
}
