
package com.xiaomi.xms.sales;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.xiaomi.xms.sales.activity.AddressActivity;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.MainActivity;
import com.xiaomi.xms.sales.activity.OrderListActivity;
import com.xiaomi.xms.sales.activity.ProductActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class ShopUrlResolver {
    public static final String MOBILE_SHOP_HOST = "m.xiaomi.com";

    private static Context mContext;

    public static ShopUrl parse(Context context, String url) {
        mContext = context;
        ShopUrl urlObj;
        try {
            urlObj = new ShopUrl(url);
        } catch (Exception e) {
            return null;
        }
        if (TextUtils.equals(MOBILE_SHOP_HOST, urlObj.getHost())) {
            return urlObj;
        }
        return null;
    }

    public static class ShopUrl {
        private static final int URL_TYPE_OLD = 1;
        private static final int URL_TYPE_NEW = 2;
        private static final String DEFAULT_PARAM = "default_param";
        private String mProtocol;
        private String mHost;
        private int mPort;
        private String mPath;
        private String mQuery;
        private String mRef;
        private String mModel;
        private String mAction;
        private ContentValues mParams;
        private int mUrlType = 1;

        public ShopUrl(String url) throws Exception {
            URL urlObj = new URL(url);
            mProtocol = urlObj.getProtocol();
            mHost = urlObj.getHost();
            mPort = urlObj.getPort();
            mPath = urlObj.getPath();
            mQuery = urlObj.getQuery();
            mRef = urlObj.getRef();
            mParams = new ContentValues();
            parseModAct();
            run();
        }

        private void run() {
            if ("home".equalsIgnoreCase(getModel())) {
                if ("yuyue".equalsIgnoreCase(getAction())) {
                    Utils.Preference.removePref(mContext, Constants.Prefence.PREF_ACTIVITY_VERSION);
                    ((BaseActivity) mContext).checkActivity();
                }
            } else if ("product".equalsIgnoreCase(getModel())) {
                if ("category".equalsIgnoreCase(getAction())) {
                    MainActivity.launchMain(mContext, MainActivity.FRAGMENT_TAG_CATEGORY);
                } else if ("list".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, ProductActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_ID, getParamByKey("cate_id"));
                    mContext.startActivity(intent);
                } else if ("view".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, getParamByKey("product_id"));
                    mContext.startActivity(intent);
                }
            } else if ("xiaomi".equalsIgnoreCase(getModel())) {
                if ("mi2s".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, "1538");
                    mContext.startActivity(intent);
                } else if ("mi2a".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, "1708");
                    mContext.startActivity(intent);
                } else if ("mi2".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, "1468");
                    mContext.startActivity(intent);
                } else if ("m1sy".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, "1336");
                    mContext.startActivity(intent);
                } else if ("m1s".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, "1338");
                    mContext.startActivity(intent);
                } else if ("box".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, ProductDetailsActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, "1731");
                    mContext.startActivity(intent);
                }
            } else if ("account".equalsIgnoreCase(getModel())) {
                if ("index".equalsIgnoreCase(getAction())) {
//                    MainActivity.launchMain(mContext, MainActivity.FRAGMENT_TAG_ACCOUNT);
                }
            } else if ("shopping".equalsIgnoreCase(getModel())) {
                Intent intent = new Intent(mContext, ShoppingActivity.class);
                mContext.startActivity(intent);
            } else if ("order".equalsIgnoreCase(getModel())) {
                if ("list".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, OrderListActivity.class);
                    intent.setAction(Constants.Intent.ACTION_ORDER_LIST);
                    intent.putExtra(Constants.Intent.EXTRA_ORDER_LIST_TYPE, getParamByKey("type"));
                    mContext.startActivity(intent);
                } else if ("view".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, OrderListActivity.class);
                    intent.setAction(Constants.Intent.ACTION_VIEW_ORDER);
                    intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID,
                            getParamByKey("order_id"));
                    mContext.startActivity(intent);
                }
            } else if ("address".equalsIgnoreCase(getModel())) {
                if ("list".equalsIgnoreCase(getAction())) {
                    Intent intent = new Intent(mContext, AddressActivity.class);
                    intent.setAction(Constants.Intent.ACTION_EDIT_ADDRESS);
                    mContext.startActivity(intent);
                }
            }
        }

        private void parseOldM() {
            List<NameValuePair> list;
            try {
                list = URLEncodedUtils.parse(new URI("http://xiaomi.com/?" + mRef), "utf-8");
            } catch (URISyntaxException e) {
                return;
            }
            for (NameValuePair pair : list) {
                mParams.put(pair.getName(), pair.getValue());
            }
            mModel = mParams.getAsString("ac");
            mAction = mParams.getAsString("op");
        }

        private void parseNewM() {
            String[] params = mRef.split("/");
            mModel = params[0];
            mAction = params[1];
            if (params.length > 2) {
                mParams.put(DEFAULT_PARAM, params[2]);
            }
        }

        private void parseModAct() {
            if (mRef.contains("ac=") && mRef.contains("op=")) {
                parseOldM();
            } else {
                parseNewM();
                mUrlType = URL_TYPE_NEW;
            }
        }

        public String getProtocol() {
            return mProtocol;
        }

        public String getHost() {
            return mHost;
        }

        public int getPort() {
            return mPort;
        }

        public String getPath() {
            return mPath;
        }

        public String getQuery() {
            return mQuery;
        }

        public String getRef() {
            return mRef;
        }

        public String getModel() {
            return mModel;
        }

        public String getAction() {
            return mAction;
        }

        public String getParamByKey(String key) {
            if (mParams.containsKey(key)) {
                return mParams.getAsString(key);
            }
            if (mUrlType == URL_TYPE_NEW) {
                return mParams.getAsString(DEFAULT_PARAM);
            }
            return null;
        }
    }
}
