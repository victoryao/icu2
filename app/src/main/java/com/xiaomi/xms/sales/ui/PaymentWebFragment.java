package com.xiaomi.xms.sales.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.PaymentActivity;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.List;

public class PaymentWebFragment extends BaseWebFragment {
    private static final String TAG = "PaymentWebFragment";
    private String mUrl;
    private String mOrderId;
    private double mFee;
    private String mPayType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        Bundle bundle = getArguments();
        mUrl = bundle.getString(Constants.Intent.EXTRA_PAYMENT_URL);
        mOrderId = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
        mFee = bundle.getDouble(Constants.Intent.EXTRA_PAYMENT_FEE);
        mPayType = bundle.getString(Constants.Intent.EXTRA_PAYMENT_TYPE);
        mWebView.requestFocus();
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                LogUtil.d(TAG, "shouldOverrideUrlLoading:" + url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                LogUtil.d(TAG, "onPageFinished:" + url);
                Uri uri = Uri.parse(url);
                if (uri != null && TextUtils.equals(uri.getPath(), HostManager.URL_PAY_RESULT_PATH)) {
                    onWebPayFinished();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                LogUtil.d(TAG, "onPageStarted:" + url);
            }
        });

        if (!TextUtils.isEmpty(mUrl) && !TextUtils.isEmpty(mOrderId)) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(Parameters.Keys.CLIENT_ID,
                    Parameters.Values.CLIENT_ID));
            params.add(new BasicNameValuePair(Parameters.Keys.ORDER_ID, mOrderId));
            params.add(new BasicNameValuePair(Parameters.Keys.PAY_ONLINE_BANK, mPayType));
            mWebView.loadUrl(String.format("%s?%s", mUrl,
                    URLEncodedUtils.format(params, HTTP.UTF_8)));
        }
        return v;
    }

    private void onWebPayFinished() {
        LogUtil.d(TAG, "onPayFinished");
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
        bundle.putDouble(Constants.Intent.EXTRA_PAYMENT_FEE, mFee);
        ((PaymentActivity)getActivity()).showPaymentConfirmDialog(bundle);
    }

    @Override
    public boolean handleBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            onWebPayFinished();
        }
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.payment_web;
    }
}
