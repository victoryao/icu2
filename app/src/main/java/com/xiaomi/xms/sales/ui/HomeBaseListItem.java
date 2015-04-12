
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.CampaignActivity;
import com.xiaomi.xms.sales.activity.FullScreenImageActivity;
import com.xiaomi.xms.sales.activity.ProductActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.model.HomeInfo;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public abstract class HomeBaseListItem extends BaseListItem<HomeInfo> {
    private static final String TAG = "HomeListItem";

    public HomeBaseListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static void viewProductDetail(Context context, HomeInfo homeInfo, boolean fullScreen) {
        if (homeInfo != null && context != null) {
            int itemType = homeInfo.getItemType();
            LogUtil.d(TAG, "itemType: " + itemType);
            if (HomeInfo.ITEM_TYPE_AD == itemType) {
                if (!LoginManager.getInstance().hasLogin()) {
                    ((BaseActivity) context).gotoAccount();
                    return;
                }
                LogUtil.d(TAG, "AD url is: " + homeInfo.getActivityUrl());
                String url = homeInfo.getActivityUrl();
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = transIntentFromUrl(context, url, homeInfo.getProductName());
                    if (intent != null) {
                        context.startActivity(intent);
                    }
                }
            } else if (HomeInfo.ITEM_TYPE_FULLSCREEN_AD == itemType) {
                if (!LoginManager.getInstance().hasLogin()) {
                    ((BaseActivity) context).gotoAccount();
                    return;
                }
                LogUtil.d(TAG, "AD url is: " + homeInfo.getActivityUrl());
                ((BaseActivity) context).startCampaignActivityWithAnimation(homeInfo
                        .getActivityUrl());
            } else {
                LogUtil.d(TAG, "Goods id is:" + homeInfo.getProductId());
                if (!TextUtils.isEmpty(homeInfo.getProductId())) {
                    Intent intent = null;
                    if (HomeInfo.ITEM_TYPE_PRODUCT == itemType) {
                        if (fullScreen) {
                            intent = new Intent(context, FullScreenImageActivity.class);
                            intent.putExtra(Constants.Intent.EXTRA_GO_TO_FRAGMENT,
                                    FullScreenImageActivity.TAG_PRODUCT_DETAIL_FULL_SCREEN_FRAGMENT);
                        }
                        else {
                            intent = new Intent(context, ProductDetailsActivity.class);
                        }
                    } else if (HomeInfo.ITEM_TYPE_MIPHONE == itemType) {
                        intent = new Intent(context, ProductDetailsActivity.class);
                        intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                        intent.putExtra(Constants.Intent.EXTRA_MIPHONE_NAME,
                                homeInfo.getProductName());
                    }
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, homeInfo.getProductId());
                    context.startActivity(intent);
                }
            }
        }
    }

    /**
     * 根据URL解析应该跳转的activity
     * 
     * @param url
     * @param name
     * @return
     */
    private static Intent transIntentFromUrl(Context context, String url, String name) {
        Uri uri = Uri.parse(url.replace(Constants.MobileWebUri.FRAGMENT_SEPARATOR,
                Constants.MobileWebUri.QUERY_SEPARATOR));
        Log.d(TAG, "query:" + uri.getQuery());
        Intent intent = null;
        if (TextUtils.equals(uri.getQueryParameter(Constants.MobileWebUri.QUERY_PARAM_ACTION),
                Constants.MobileWebUri.QUERY_PARAM_ACTION_PRODUCT))
        {
            if (TextUtils.equals(uri.getQueryParameter(Constants.MobileWebUri.QUERY_PARAM_OPTION),
                    Constants.MobileWebUri.QUERY_PARAM_OPTION_LIST)) {
                String cateId = uri
                        .getQueryParameter(Constants.MobileWebUri.QUERY_PARAM_OPTION_LIST_ID);
                if (!TextUtils.isEmpty(cateId)) {
                    intent = new Intent(context, ProductActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_ID, cateId);
                    name = TextUtils.isEmpty(name) ? context.getResources().getString(
                            R.string.category_default_name) : name;
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_NAME, name);
                    return intent;
                }
            } else if (TextUtils.equals(
                    uri.getQueryParameter(Constants.MobileWebUri.QUERY_PARAM_OPTION),
                    Constants.MobileWebUri.QUERY_PARAM_OPTION_VIEW)) {
                String productId = uri
                        .getQueryParameter(Constants.MobileWebUri.QUERY_PARAM_OPTION_VIEW_ID);
                if (!TextUtils.isEmpty(productId)) {
                    intent = new Intent(context, ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, productId);
                    return intent;
                }
            }
        }
        if (intent == null) {
            // intent = new Intent(context, CampaignActivity.class);
            // intent.putExtra(Constants.Intent.EXTRA_COMPAIGN_URL, url);
            CampaignActivity.startActivityStandard((BaseActivity) context, url);
        }
        return intent;
    }
}
