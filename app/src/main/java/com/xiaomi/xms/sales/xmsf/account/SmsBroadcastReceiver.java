package com.xiaomi.xms.sales.xmsf.account;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import com.xiaomi.xms.sales.util.LogUtil;

import java.util.concurrent.CountDownLatch;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsBroadcastReceiver";

    private volatile CountDownLatch countDownLatch;

    private volatile int result;

    @Override
    public void onReceive(Context context, Intent intent) {
        result = getResultCode();
        LogUtil.d(TAG, "sms sent, result:" + result);
        countDownLatch.countDown();
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public int getResult() {
        return result;
    }

    public void reset() {
        result = SmsManager.RESULT_ERROR_NO_SERVICE;
    }
}
