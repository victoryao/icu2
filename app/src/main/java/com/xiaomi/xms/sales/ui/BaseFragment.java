package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;

import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.BaseActivity.OnRefreshListener;
import com.xiaomi.xms.sales.loader.BaseLoader;
import com.xiaomi.xms.sales.loader.BaseResult;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;

public abstract class BaseFragment extends Fragment implements OnRefreshListener {
    private static final String TAG = "BaseFragment";
    private NetworkConnectivityChangedReceiver mNetworkConnectivityReceiver;
    protected boolean mNetworkConnected;
    protected BaseLoader<? extends BaseResult> mLoader;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        registerConnectivityReceiver();
        mNetworkConnected = Utils.Network.isNetWorkConnected(getActivity());
        ((BaseActivity) getActivity()).registerRefreshListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterConnectivityReceiver();
        ((BaseActivity) getActivity()).unregisterRefreshListener(this);
    }

    /**
     * 网络从未连接状态到连接的时候调用该方法
     * @param type 网络类型，用{@link #ConnectivityManager} 中标识的Type来判断连接的网络
     */
    protected void onNetworkConnected(int type) {
        if (mLoader != null) {
            mLoader.reload();
        }
    }

    private void registerConnectivityReceiver() {
        LogUtil.d(TAG, "Register network connectivity changed receiver");
        if (mNetworkConnectivityReceiver == null) {
            mNetworkConnectivityReceiver = new NetworkConnectivityChangedReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mNetworkConnectivityReceiver, filter);
    }

    private void unregisterConnectivityReceiver() {
        LogUtil.d(TAG, "Unregister network connectivity changed receiver");
        getActivity().unregisterReceiver(mNetworkConnectivityReceiver);
    }

    private class NetworkConnectivityChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = Utils.Network.isNetWorkConnected(getActivity());
            if (!mNetworkConnected && isConnected) {
                onNetworkConnected(Utils.Network.getActiveNetworkType(getActivity()));
            }
            mNetworkConnected = isConnected;
        }
    }

    @Override
    public void onRefresh() {
        if (isVisible() && mLoader != null) {
            LogUtil.d(TAG, getTag() + " fragment was refreshed");
            mLoader.reload();
        }
    }

}
