
package com.xiaomi.xms.sales.xmsf.account.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.xmsf.account.SmsBroadcastReceiver;
import com.xiaomi.xms.sales.xmsf.account.exception.InvalidResponseException;
import com.xiaomi.xms.sales.xmsf.account.utils.CloudHelper;
import com.xiaomi.xms.sales.xmsf.account.utils.SysHelper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RegisterPasswordFragment extends StepsFragment {

    private static final String TAG = "RegisterPasswordFragment";

    private static final int ERROR_NETWORK = 1;
    private static final int ERROR_SERVER = 2;
    private static final int ERROR_SEND_SMS_FAIL = 3;
    private static final int ERROR_PHONE_NULL = 4;

    private static final String ACTION_REG_SMS_SENT = "com.xiaomi.xmsf.action.REG_SMS_SENT";

    /**
     * Timeout in seconds of waiting activate SMS sent status.
     */
    private static final int ROLL_SMS_SEND_TIMEOUT = 10;

    private String mNewAccountType;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater
                .inflate(R.layout.micloud_input_password, container, false);
        mPasswordView = (EditText) v.findViewById(R.id.ev_password);
        mPasswordConfirmView = (EditText) v.findViewById(R.id.ev_password_confirm);
        mPasswordConfirmView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    triggerNextStep();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.requestFocus();
        mPasswordView
                .setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            String password = mPasswordView.getText()
                                    .toString();
                            if (TextUtils.isEmpty(password)) {
                                mPasswordView
                                        .setError(getErrorSpanString(
                                        R.string.micloud_error_empty_pwd));
                            } else if (!SysHelper
                                    .checkPasswordPattern(password)) {
                                mPasswordView.setError(
                                        getErrorSpanString(
                                        R.string.micloud_error_illegal_pwd));
                            }
                        }
                    }
                });
        return v;
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
    public void onResume() {
        super.onResume();
        displaySoftInputIfNeed(mPasswordView, true);
    }

    public String getNewAccountType() {
        return mNewAccountType;
    }

    public void setNewAccountType(String newAccountType) {
        mNewAccountType = newAccountType;
    }

    public String getPassword() {
        return mPasswordView.getText().toString();
    }

    private String checkAndGetPassword() {
        final String password = mPasswordView.getText().toString();
        final String passwordConfirm = mPasswordConfirmView.getText()
                .toString();

        if (TextUtils.isEmpty(password)) {
            mPasswordView
                    .setError(getErrorSpanString(R.string.micloud_error_empty_pwd));
            mPasswordView.requestFocus();
            return null;
        } else if (!SysHelper.checkPasswordPattern(password)) {
            mPasswordView.setError(
                    getErrorSpanString(R.string.micloud_error_illegal_pwd));
            mPasswordView.requestFocus();
            return null;
        } else if (!password.equals(passwordConfirm)) {
            mPasswordConfirmView.setError(getErrorSpanString(
                    R.string.micloud_password_error_inconsistent));
            mPasswordView.requestFocus();
            return null;
        } else {
            // passwords are ok
            return password;
        }
    }

    @Override
    protected void onButtonNextClicked() {
        final String password = checkAndGetPassword();
        if (TextUtils.isEmpty(password)) {
            // 检查未通过
            return;
        }

        if (Constants.Account.REG_TYPE_PHONE_NUMBER.equals(mNewAccountType)) {
            BaseAlertDialog dialog = new BaseAlertDialog(getActivity());
            dialog.setMessage(R.string.micloud_val_sms_alert);
            dialog.setNegativeButton(android.R.string.cancel, null);
            dialog.setPositiveButton(android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // async send an SMS
                            RegBySmsTask task = new RegBySmsTask(new Runnable() {
                                @Override
                                public void run() {
                                    RegisterSmsSentFragment f = new RegisterSmsSentFragment();
                                    replaceToFragment(f, true);
                                    // startQueryActivation();
                                    getActivity().setResult(Activity.RESULT_OK);
                                }
                            });
                            task.execute(password);
                        }
                    });
            dialog.show();
        } else if (Constants.Account.REG_TYPE_EMAIL.equals(mNewAccountType)) {
            Context context = getActivity();
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

            Bundle arg = getArguments();
            if (arg != null) {
                final String email = arg.getString("email");
                RegByEmailTask task = new RegByEmailTask(new Runnable() {
                    @Override
                    public void run() {
                        // show sent email view
                        RegisterEmailSentFragment f = new RegisterEmailSentFragment();
                        Bundle arg = new Bundle();
                        arg.putString("email", email);
                        f.setArguments(arg);
                        replaceToFragment(f, true);
                        // startQueryActivation();
                        getActivity().setResult(Activity.RESULT_OK);
                    }
                });
                task.execute(email, password);
            } else {
                Log.w(TAG, "no argument found");
            }

        }
    }

    private class RegBySmsTask extends AsyncTask<String, Void, Integer> {
        private ProgressDialog mProgressDialog;
        private Runnable mSuccessRunnable;

        private RegBySmsTask(Runnable runnable) {
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
                handleRegisterFailed(result);
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            final Context context = getActivity();
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            final String password = params[0];
            final String deviceId = tm.getDeviceId();
            final String imsi = tm.getSubscriberId();
            // check the phone if is previous registered

            Intent sentIntent = new Intent(ACTION_REG_SMS_SENT);
            sentIntent.setPackage(context.getPackageName());
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, sentIntent,
                    PendingIntent.FLAG_ONE_SHOT);

            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_REG_SMS_SENT);

            SmsBroadcastReceiver receiver = new SmsBroadcastReceiver();

            CountDownLatch countDownLatch = new CountDownLatch(1);
            receiver.setCountDownLatch(countDownLatch);
            receiver.reset();

            context.registerReceiver(receiver, filter);

            CloudHelper.regBySms(context, pi, deviceId, imsi, password);
            try {
                countDownLatch.await(ROLL_SMS_SEND_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int ret = receiver.getResult();

            context.unregisterReceiver(receiver);
            return ret;
        }
    }

    private class RegByEmailTask extends AsyncTask<String, Void, Integer> {
        private ProgressDialog mProgressDialog;
        private Runnable mSuccessRunnable;

        private RegByEmailTask(Runnable runnable) {
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
                handleRegisterFailed(result);
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            final String email = params[0];
            final String password = params[1];
            try {
                String userId = CloudHelper.regByEmail(email, password);
                return Activity.RESULT_OK;
            } catch (IOException e) {
                e.printStackTrace();
                return ERROR_NETWORK;
            } catch (InvalidResponseException e) {
                e.printStackTrace();
                return ERROR_SERVER;
            }
        }
    }

    // TODO(chenyinli) handle other errors
    private void handleRegisterFailed(int error) {
        String reason;
        switch (error) {
            case ERROR_NETWORK: {
                reason = getString(R.string.error_network);
                break;
            }
            case ERROR_SERVER: {
                reason = getString(R.string.error_server);
                break;
            }
            case ERROR_SEND_SMS_FAIL: {
                reason = getString(R.string.error_no_sms_service);
                break;
            }
            case ERROR_PHONE_NULL: {
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
}
