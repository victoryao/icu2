
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.CampaignActivity;
import com.xiaomi.xms.sales.activity.FCodeActivity;
import com.xiaomi.xms.sales.activity.MainActivity;
import com.xiaomi.xms.sales.activity.OrderListActivity;
import com.xiaomi.xms.sales.activity.ProductActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseWebView;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;
import com.xiaomi.xms.sales.xmsf.account.LoginManager.AccountListener;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseWebFragment extends BaseFragment implements AccountListener {
    private final static String TAG = "BaseWebFragment";
    private final static int FILECHOOSER_RESULTCODE = 1;
    protected BaseWebView mWebView;
    protected ProgressBar mProgressBar;
    private WebViewLoadingListener mLoadingListener;
    private Handler mHandler;
    private ValueCallback<Uri> mUploadMessage;

    public interface WebViewLoadingListener {
        public void onLoadFinished();
    }

    public BaseWebFragment() {
        LoginManager.getInstance().addLoginListener(this);
        mHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LoginManager.getInstance().removeLoginListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        mWebView = (BaseWebView) view.findViewById(R.id.browser);
        initWebViewSettings();
        mProgressBar = (ProgressBar) view.findViewById(R.id.browser_progress_bar);
        return view;
    }

    protected void initWebViewSettings() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setDatabaseEnabled(true);
        String dir = getActivity().getApplicationContext()
                .getDir("database", Context.MODE_PRIVATE).getPath();
        mWebView.getSettings().setDatabasePath(dir);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebViewClient(new ShopWebViewClient());
        mWebView.setWebChromeClient(new ShopWebChromeClient());
        mWebView.addJavascriptInterface(new WebEvent(), "WE");
        mWebView.requestFocus();
    }

    protected abstract int getLayoutId();

    public void setWebViewLoadingListener(WebViewLoadingListener l) {
        mLoadingListener = l;
    }

    private class ShopWebViewClient extends WebViewClient {
        private boolean mReceivedError;

        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mReceivedError = true;
            LogUtil.w(TAG, "onReceivedError.");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // 注意：如果页面加载失败，mLoadingListener不会触发，涉及所有webview，以后有变化再进行修改。
            if (mLoadingListener != null && !mReceivedError) {
                mLoadingListener.onLoadFinished();
            }
            LogUtil.w(TAG, "onPageFinished:" + url);
        }
    }

    private class ShopWebChromeClient extends WebChromeClient {
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i, getString(R.string.web_choose_file)),
                    FILECHOOSER_RESULTCODE);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooser(uploadMsg, acceptType);
        }

        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (mProgressBar == null) {
                return;
            }
            mProgressBar.setProgress(progress);
            if (progress == 100) {
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    public boolean handleBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void onLogin(String userId, String authToken, String security) {
        LogUtil.d(TAG, "event login");
        mWebView.reload();
    }

    @Override
    public void onInvalidAuthonToken() {
        return;
    }

    @Override
    public void onLogout() {
        LogUtil.d(TAG, "event logout");
        mWebView.reload();
    }

    public void loadUrl(String url) {
        LogUtil.d(TAG, "loadUrl: " + url);
        mWebView.loadUrl(url);
    }

    public void clearHistory() {
        mWebView.clearHistory();
    }

    private class WebEvent {
        private void closeWebView() {
            LogUtil.d(TAG, "close Web");
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

        public void logHTML(String html) {
            LogUtil.d(TAG, "html:" + html);
        }

        public boolean trigger(String eventName, String data) {
            LogUtil.d(TAG, "get event '" + eventName + "', data:" + data);
            return handleEvent(eventName, data);
        }

        private boolean handleEvent(String event, String data) {
            if (Constants.WebView.EVENT_LOGIN.equals(event)) {
                LogUtil.d(TAG, "enter login");
                return doLogin();
            } else if (Constants.WebView.EVENT_SHOPPING.equals(event)) {
                doViewShopping();
            } else if (Constants.WebView.EVENT_PRODUCT.equals(event)) {
                doViewProduct(data);
            } else if (Constants.WebView.EVENT_NEW_WEB.equals(event)) {
                doNewWeb(data);
            } else if (Constants.WebView.EVENT_GO_HOME.equals(event)) {
                doViewHome();
            } else if (Constants.WebView.EVENT_FCODE.equals(event)) {
                doFcode(data);
            } else if (Constants.WebView.EVENT_ORDERLIST.equals(event)) {
                doGoOrderList();
            } else if (Constants.WebView.EVENT_CHECK_INSTALL.equals(event)) {
                return true;
            } else if (Constants.WebView.EVENT_CHECK_UPDATE.equals(event)) {
                doCheckUpdate();
            } else if (Constants.WebView.EVENT_IS_WIFI.equals(event)) {
                return isWIFI();
            } else if (Constants.WebView.EVENT_BOTTOM_STYLE.equals(event)) {
                doChangeBottomStyle(data);
            } else if (Constants.WebView.EVENT_PRODUCTLIST.equals(event)) {
                return goProductList(data);
            } else if (Constants.WebView.EVENT_MIPHONE_DETAIL.equals(event)) {
                return goMiPhoneDetail(data);
            } else if (Constants.WebView.EVENT_SHOW_TITLE_BAR.equals(event)) {
                showTitleBar(data);
            } else if (Constants.WebView.EVENT_HIDE_TITLE_BAR.equals(event)) {
                hideTitleBar();
            } else {
                return false;
            }
            return true;
        }

        private void hideTitleBar() {
            Activity activity = getActivity();
            if (activity instanceof CampaignActivity) {
                ((CampaignActivity) activity).hideTitleBar();
            }
        }

        private void showTitleBar(String title) {
            Activity activity = getActivity();
            if (activity instanceof CampaignActivity) {
                ((CampaignActivity) activity).showTitleBar(title);
            }
        }

        private boolean goMiPhoneDetail(String jsonStr) {
            String miPhoneId = null;
            String miPhoneName = null;
            try {
                JSONObject json = new JSONObject(jsonStr);
                miPhoneId = json.optString("id");
                miPhoneName = json.optString("name");
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
            if (!TextUtils.isEmpty(miPhoneName)) {
                intent.putExtra(Constants.Intent.EXTRA_MIPHONE_NAME, miPhoneName);
            }

            if (!TextUtils.isEmpty(miPhoneId)) {
                intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, miPhoneId);
                intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                startActivity(intent);
                return true;
            }
            return false;
        }

        private boolean goProductList(String jsonStr) {
            String cateId = null;
            String name = null;
            try {
                JSONObject json = new JSONObject(jsonStr);
                cateId = json.optString("id");
                name = json.optString("name");
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            Intent intent = new Intent(getActivity(), ProductActivity.class);
            if (!TextUtils.isEmpty(cateId)) {
                intent.putExtra(Constants.Intent.EXTRA_CATEGORY_ID, cateId);
            }
            if (!TextUtils.isEmpty(name)) {
                intent.putExtra(Constants.Intent.EXTRA_CATEGORY_NAME, name);
            }
            getActivity().startActivity(intent);
            return true;
        }

        private boolean isWIFI() {
            return Utils.Network.isWifiConnected(getActivity());
        }

        private void doCheckUpdate() {
            ((BaseActivity) getActivity()).checkUpdate(true);
        }

        private void doChangeBottomStyle(String style) {
            BaseActivity base = ((BaseActivity) getActivity());
            if (base != null) {
                if (base instanceof CampaignActivity) {
                    ((CampaignActivity) base).setBottomBarStyle(style);
                }
            }
        }

        private void doGoOrderList() {
            Intent intent = new Intent(getActivity(), OrderListActivity.class);
            intent.setAction(Constants.Intent.ACTION_ORDER_LIST);
            intent.putExtra(Constants.Intent.EXTRA_ORDER_LIST_TYPE, Tags.Order.ORDER_STATUS_OPEN);
            startActivity(intent);
        }

        private void doFcode(String fcode) {
            Intent intent = new Intent(getActivity(), FCodeActivity.class);
            intent.putExtra(Constants.Intent.EXTRA_CHECKCODE_FCODE, fcode);
            startActivity(intent);
        }

        private void doNewWeb(String url) {
            Uri contentUrl = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, contentUrl);
            startActivity(intent);
        }

        private void doViewShopping() {
            Intent intent = new Intent(getActivity(), ShoppingActivity.class);
            startActivity(intent);
        }

        private void doViewHome() {
            closeWebView();
            MainActivity.launchMain(getActivity(), MainActivity.FRAGMENT_TAG_HOME);
        }

        private boolean doLogin() {
            LogUtil.d(TAG, "start activity");
            if (LoginManager.getInstance().hasLogin()) {
                return false;
            }
            ((BaseActivity) getActivity()).gotoAccount();
            return true;
        }

        private void doViewProduct(String productId) {
            LogUtil.d(TAG, "view product detail");
            Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
            intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, productId);
            startActivity(intent);
        }
    }

    @Override
    public void onRefresh() {
        if (isVisible()) {
            LogUtil.d(TAG, getTag() + " fragment was refreshed");
            mWebView.reload();
        }
    }

    @Override
    protected void onNetworkConnected(int type) {
        mWebView.reload();
    }
}
