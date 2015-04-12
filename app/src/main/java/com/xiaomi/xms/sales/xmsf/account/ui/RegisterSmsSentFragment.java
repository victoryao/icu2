package com.xiaomi.xms.sales.xmsf.account.ui;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.xmsf.account.exception.InvalidResponseException;
import com.xiaomi.xms.sales.xmsf.account.utils.CloudHelper;

import java.io.IOException;

public class RegisterSmsSentFragment extends StepsFragment {
    private static final String TAG = "RegisterSmsSentFragment";

    private TextView mStatusTextView;
    private ProgressBar mProgressBar;

    private static final int ERROR_NETWORK  = 1;
    private static final int ERROR_REGISTER = 2;

    /**
     * Intervals in milliseconds of rolling sms status upon server.
     */
    public static final int[] ROLL_SMS_STATUS_INTERVALS = new int[] {
            1000, 2000, 5000, 10000, 20000, 60000
    };

    public static final int[] ROLL_REG_STATUS_INTERVALS = new int[] {
            5000, 5000, 20000, 60000
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.micloud_sms_sent, container, false);
        getActivity().setTitle(R.string.title_activate);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStatusTextView = (TextView) view.findViewById(R.id.status_text);
        mStatusTextView.setVisibility(View.GONE);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        QueryActivationTask task = new QueryActivationTask();
        task.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        displaySoftInputIfNeed(getView(), false);
    }

    @Override
    protected void onButtonFinishClicked() {
        getActivity().finish();
    }

    class QueryActivationTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            mStatusTextView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);

            mStatusTextView.setText(R.string.micloud_active_sms_status_wait);
            mButtonFinish.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Integer result) {
            mProgressBar.setVisibility(View.GONE);
            if (result == Activity.RESULT_OK) {
                mStatusTextView.setText(R.string.micloud_active_sms_status_success);
            } else if (result == ERROR_NETWORK) {
                mStatusTextView.setText(R.string.micloud_active_sms_status_network);
            } else if (result == ERROR_REGISTER) {
                mStatusTextView.setText(R.string.micloud_active_sms_status_fail);
            }
            mButtonFinish.setEnabled(true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            for (int wait : ROLL_SMS_STATUS_INTERVALS) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    boolean success = check();
                    if (success) {
                        return Activity.RESULT_OK;
                    }
                } else {
                    return ERROR_NETWORK;
                }
            }
            return ERROR_REGISTER;
        }

        private boolean check() {
            Context context = getActivity();

            String phone = null;
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            final String deviceId = tm.getDeviceId();
            final String imsi = tm.getSubscriberId();
            try {
                phone = CloudHelper.queryPhone(deviceId, imsi);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidResponseException e) {
                e.printStackTrace();
            }

            if (TextUtils.isEmpty(phone)) {
                return false;
            }

            LogUtil.d(TAG, "get phone:" + phone);

            // query user id
            String userId = null;
            try {
                userId = CloudHelper.getUserIdForPhone(phone);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidResponseException e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(userId)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
