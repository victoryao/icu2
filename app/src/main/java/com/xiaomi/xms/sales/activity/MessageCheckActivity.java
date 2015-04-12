
package com.xiaomi.xms.sales.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.RequestLoader;
import com.xiaomi.xms.sales.loader.RequestLoader.Result;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;

public class MessageCheckActivity extends BaseActivity {
    private static final String TAG = "MessageCheckActivity";
    private static final int REQUEST_CHECKCODE_LOADER = 1;
    private static final int REQUEST_SEND_MESSAGE_LOADER = 0;

    private RequestLoader mCheckCodeLoader;
    private RequestLoader mSendMessageLoader;
    private EditText mVerifiCode;
    private Button mSubmit;
    private Button mSendMessageSubmit;
    private TextView mPromptText;

    private String mOldTel;
    private String mOrderId;
    private TimeCount mTime;
    private Long mOverTimeLong = (long) 0;
    private boolean startDownCount;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCustomContentView(R.layout.order_edit_secure_activity);
        mVerifiCode = (EditText) findViewById(R.id.secure_authentication);
        mSubmit = (Button) findViewById(R.id.submit);
        mSendMessageSubmit = (Button) findViewById(R.id.send_message_btn);
        mPromptText = (TextView) findViewById(R.id.prompt_send_text);

        mOldTel = getIntent().getStringExtra(Constants.Intent.EXTRA_ORDER_EDIT_OLDTEL);
        mOrderId = getIntent().getStringExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
        if (!TextUtils.isEmpty(mOldTel)) {
            mPromptText.setText(getString(R.string.order_edit_prompt_message,
                    mOldTel.substring(0, 3) + "****" + mOldTel.substring(7)));
        }

        mSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = mVerifiCode.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    ToastUtil.show(MessageCheckActivity.this, R.string.order_edit_code_not_null);
                    return;
                }
                getSupportLoaderManager().initLoader(REQUEST_CHECKCODE_LOADER, null,
                        mRequestCheckCodeCallback);
                if (mCheckCodeLoader != null) {
                    Request request = new Request(HostManager.getCheckMessageCode());
                    request.addParam(Tags.EditOrder.CHECKCODE, code);
                    request.addParam(Tags.EditOrder.ORDER_ID, mOrderId);
                    mCheckCodeLoader.load(REQUEST_CHECKCODE_LOADER, request);
                }
            }
        });

        mSendMessageSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportLoaderManager().initLoader(REQUEST_SEND_MESSAGE_LOADER, null,
                        mRequestSendMessageCallback);
                if (mSendMessageLoader != null) {
                    startDownCount = true;
                    Request request = new Request(HostManager.getSendMessage());
                    request.addParam(Tags.EditOrder.TEL, mOldTel);
                    request.addParam(Tags.EditOrder.ORDER_MODIFY, "1");
                    request.addParam(Tags.EditOrder.ORDER_ID, mOrderId);
                    mSendMessageLoader.load(REQUEST_SEND_MESSAGE_LOADER, request);
                }
            }
        });

    }

    private LoaderCallbacks<RequestLoader.Result> mRequestCheckCodeCallback = new LoaderCallbacks<RequestLoader.Result>() {
        @Override
        public void onLoaderReset(Loader<Result> loader) {
        }

        @Override
        public void onLoadFinished(Loader<Result> loader, Result result) {
            getSupportLoaderManager().destroyLoader(REQUEST_CHECKCODE_LOADER);
            if (result != null) {
                if (Tags.isJSONResultOK(result.mData)) {
                    handleResult(true);
                    Utils.Preference.setLongPref(MessageCheckActivity.this,
                            Constants.Prefence.PREF_KEY_MESSAGE_OVER_TIME + mOrderId, (long) 0);
                } else {
                    handleResult(false);
                    if (result.mData != null) {
                        ToastUtil.show(MessageCheckActivity.this,
                                result.mData.optString(Tags.DESCRIPTION, "服务异常"));
                    }
                }
            }
        }

        @Override
        public Loader<Result> onCreateLoader(int type, Bundle bundle) {
            mCheckCodeLoader = new RequestLoader(MessageCheckActivity.this);
            return mCheckCodeLoader;
        }
    };

    private LoaderCallbacks<RequestLoader.Result> mRequestSendMessageCallback = new LoaderCallbacks<RequestLoader.Result>() {
        @Override
        public void onLoaderReset(Loader<Result> loader) {
        }

        @Override
        public void onLoadFinished(Loader<Result> loader, Result result) {
            getSupportLoaderManager().destroyLoader(REQUEST_SEND_MESSAGE_LOADER);
            if (result != null) {
                if (Tags.isJSONResultOK(result.mData)) {
                    ToastUtil.show(MessageCheckActivity.this, R.string.order_code_sent);
                    if (result.mData != null) {
                        JSONObject dataJson = result.mData.optJSONObject("data");
                        if (dataJson != null) {
                            long countDownTime = dataJson.optLong("countDownTime");
                            mTime = new TimeCount(countDownTime * 1000, 1000);
                        } else {
                            mTime = new TimeCount(300000, 1000);
                        }
                        mTime.start();
                    }
                } else {
                    if (result.mData != null) {
                        String errorDesc = result.mData.optString(Tags.DESCRIPTION);
                        if (TextUtils.isEmpty(errorDesc)) {
                            ToastUtil.show(MessageCheckActivity.this,
                                    R.string.order_code_sent_error);
                        } else {
                            ToastUtil.show(MessageCheckActivity.this, errorDesc);
                        }
                    }
                }
            }
        }

        @Override
        public Loader<Result> onCreateLoader(int type, Bundle bundle) {
            mSendMessageLoader = new RequestLoader(MessageCheckActivity.this);
            return mSendMessageLoader;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.order_edit_secure_title);
        long currentTime = System.currentTimeMillis();
        long backTime = Utils.Preference.getLongPref(this,
                Constants.Prefence.PREF_KEY_CURRENT_TIME + mOrderId, 0);
        long overTime = Utils.Preference.getLongPref(this,
                Constants.Prefence.PREF_KEY_MESSAGE_OVER_TIME + mOrderId, 0);
        if (overTime > 0 && overTime > currentTime - backTime) {
            mTime = new TimeCount(overTime - (currentTime - backTime), 1000);
            mTime.start();
        }
    }

    private void handleResult(boolean intentResult) {
        if (intentResult) {
            Intent data = new Intent();
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            mSendMessageSubmit.setText(getString(R.string.order_send_message_submit));
            mSendMessageSubmit.setTextColor(getResources().getColor(R.color.primary_text_color));
            mSendMessageSubmit.setClickable(true);
            cancel();
            mOverTimeLong = (long) 0;
            mTime = null;
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            mSendMessageSubmit.setClickable(false);
            mSendMessageSubmit.setTextColor(getResources()
                    .getColor(R.color.secondary_text_color));
            mSendMessageSubmit.setText(getString(R.string.order_retransmission_submit,
                    millisUntilFinished / 1000));
            mOverTimeLong = millisUntilFinished;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (startDownCount) {
            startDownCount = false;
            Utils.Preference.setLongPref(this, Constants.Prefence.PREF_KEY_CURRENT_TIME + mOrderId,
                    System.currentTimeMillis());
            Utils.Preference.setLongPref(this,
                    Constants.Prefence.PREF_KEY_MESSAGE_OVER_TIME + mOrderId,
                    mOverTimeLong);
        }
        mTime = null;
        ArrayList<String> list = Utils.Preference.getAllPreferenceKey(this);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).contains(Constants.Prefence.PREF_KEY_CURRENT_TIME)) {
                    String orderIdString = list.get(i).substring(
                            Constants.Prefence.PREF_KEY_CURRENT_TIME.length());
                    long overtime = Utils.Preference.getLongPref(this,
                            Constants.Prefence.PREF_KEY_MESSAGE_OVER_TIME + orderIdString, 0);
                    if (Utils.Preference.getLongPref(this, list.get(i), 0) + overtime <= System
                            .currentTimeMillis()) {
                        Utils.Preference.removePref(this,
                                Constants.Prefence.PREF_KEY_MESSAGE_OVER_TIME + orderIdString);
                        Utils.Preference.removePref(this, list.get(i));
                    }
                }
            }
        }
    }

}
