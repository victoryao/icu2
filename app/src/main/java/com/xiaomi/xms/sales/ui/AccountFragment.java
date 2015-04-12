
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.OrderEditActivity;
import com.xiaomi.xms.sales.activity.OrderListActivity;
import com.xiaomi.xms.sales.loader.BaseLoader;
import com.xiaomi.xms.sales.loader.UserInfoLoader;
import com.xiaomi.xms.sales.loader.UserInfoLoader.Result;
import com.xiaomi.xms.sales.loader.UserRemindLoader;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;
import com.xiaomi.xms.sales.xmsf.account.LoginManager.AccountListener;

import java.net.URLEncoder;

public class AccountFragment extends BaseFragment implements
        LoaderCallbacks<UserInfoLoader.Result>, AccountListener {
    private final String TAG = "AccountFragment";
    private final static int USERINFO_LOADER = 1;
    private final static int USERREMIND_LOADER = 2;
    private EmptyLoadingView mLoadingView;
    private BaseLoader<?> mUserRemindLoader;
    private TextView mUserName;
    private TextView mUserOrgName;
    private View mUserExit;
    private View mOrderList;
    private View mOrderNonPayment;
    private View mExpressList;
    private View mAccountLoginContainer;
    private View mAccountViewNotLoginContainer;
    private View mEditOrderList;
    private TextView mNoPayCount;
    private TextView mExpressCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);
        mAccountLoginContainer = view.findViewById(R.id.alread_login);
        mAccountViewNotLoginContainer = view.findViewById(R.id.not_login);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mUserName = (TextView) view.findViewById(R.id.account_user_name);
        mUserOrgName = (TextView) view.findViewById(R.id.account_user_org_name);
        mUserExit = view.findViewById(R.id.account_exit);
        mOrderList = view.findViewById(R.id.account_my_order_list);
        mOrderNonPayment = view.findViewById(R.id.account_nonpayment_list);
        mExpressList = view.findViewById(R.id.account_express_list);
        mEditOrderList = view.findViewById(R.id.account_order_edit);
        mNoPayCount = (TextView) view.findViewById(R.id.nonpayment_item_text_count);
        mExpressCount = (TextView) view.findViewById(R.id.express_item_count);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (LoginManager.getInstance().hasLogin()) {
            if (mLoader == null) {
                // getLoaderManager().initLoader(USERINFO_LOADER, null, this);
                // 拉取需要标记的数字
                getLoaderManager().initLoader(USERREMIND_LOADER, null, mUserRemindLoaderCallback);
            } else {
                mLoader.reload();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
        mAccountViewNotLoginContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity) getActivity()).gotoAccount();
            }
        });
        mOrderList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LoginManager.getInstance().hasLogin()) {
                    Intent intent = new Intent(getActivity(), OrderListActivity.class);
                    intent.setAction(Constants.Intent.ACTION_ORDER_LIST);
                    intent.putExtra(Constants.Intent.EXTRA_ORDER_LIST_TYPE,
                            Tags.Order.ORDER_STATUS_OPEN);
                    startActivity(intent);
                } else {
                    ((BaseActivity) getActivity()).gotoAccount();
                }
            }
        });
        mOrderNonPayment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LoginManager.getInstance().hasLogin()) {
                    Intent intent = new Intent(getActivity(), OrderListActivity.class);
                    intent.setAction(Constants.Intent.ACTION_ORDER_LIST);
                    intent.putExtra(Constants.Intent.EXTRA_ORDER_LIST_TYPE,
                            Tags.Order.ORDER_STATUS_WAIT_PAYMENT);
                    startActivity(intent);
                } else {
                    ((BaseActivity) getActivity()).gotoAccount();
                }
            }
        });
        mExpressList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LoginManager.getInstance().hasLogin()) {
                    Intent intent = new Intent(getActivity(), OrderListActivity.class);
                    intent.setAction(Constants.Intent.ACTION_ORDER_LIST);
                    intent.putExtra(Constants.Intent.EXTRA_ORDER_LIST_TYPE,
                            Tags.Order.ORDER_STATUS_EXPRESS);
                    startActivity(intent);
                } else {
                    ((BaseActivity) getActivity()).gotoAccount();
                }
            }
        });
        mEditOrderList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View btn) {
                if (LoginManager.getInstance().hasLogin()) {
                    Intent intent = new Intent(getActivity(), OrderEditActivity.class);
                    startActivity(intent);
                } else {
                    ((BaseActivity) getActivity()).gotoAccount();
                }
            }
        });
        mNoPayCount.setVisibility(View.GONE);
        mExpressCount.setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LoginManager.getInstance().removeLoginListener(this);
        // Delete all caches when user logout account
        if (mLoader != null && !LoginManager.getInstance().hasLogin()) {
            ((UserInfoLoader) mLoader).deleteCache();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LoginManager.getInstance().addLoginListener(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Loader<Result> onCreateLoader(int id, Bundle bundle) {
        if (id == USERINFO_LOADER) {
            mLoader = new UserInfoLoader(getActivity());
            mLoader.setProgressNotifiable(mLoadingView);
            return (Loader<Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        LogUtil.d(TAG, "result:" + data);
        if (data.mUserInfo != null) {
            mUserName.setText(data.mUserInfo.getUserName());
            mUserOrgName.setText(data.mUserInfo.getOrgName());
            showView(mAccountLoginContainer);
            hideView(mAccountViewNotLoginContainer);
            /**
             * @HACKME XM_[userId]_UN 这个cookie是用来记录用户的用户名，给Web中使用
             */
            HostManager.setCookie(getActivity(), "XM_" + LoginManager.getInstance().getUserId()
                    + "_UN", URLEncoder.encode(data.mUserInfo.getUserName()),
                    HostManager.DOMAIN_BASE);
        } else {
            mAccountLoginContainer.setVisibility(View.GONE);
            mAccountViewNotLoginContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

    private LoaderCallbacks<UserRemindLoader.Result> mUserRemindLoaderCallback = new LoaderCallbacks<UserRemindLoader.Result>() {
        @SuppressWarnings("unchecked")
        @Override
        public Loader<UserRemindLoader.Result> onCreateLoader(int id, Bundle bundle) {
            if (id == USERREMIND_LOADER) {
                mUserRemindLoader = new UserRemindLoader(getActivity());
                return (Loader<UserRemindLoader.Result>) mUserRemindLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<UserRemindLoader.Result> loader,
                UserRemindLoader.Result data) {
            getLoaderManager().destroyLoader(USERREMIND_LOADER);
            if (data != null && data.mRemindInfo != null) {
                setRemindCount(mNoPayCount, data.mRemindInfo.getUnPayCount());
                setRemindCount(mExpressCount, data.mRemindInfo.getExpressCount());
            }
        }

        @Override
        public void onLoaderReset(Loader<UserRemindLoader.Result> arg0) {
        }
    };

    private void setRemindCount(TextView tv, int count) {
        if (count > 0) {
            tv.setText(String.valueOf(count));
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLogin(String userId, String authToken, String security) {
        if (mLoader == null) {
            getLoaderManager().initLoader(USERINFO_LOADER, null, this);
        } else {
            mLoader.reload();
        }
    }

    @Override
    public void onInvalidAuthonToken() {
    }

    @Override
    public void onLogout() {
        mNoPayCount.setVisibility(View.GONE);
        mExpressCount.setVisibility(View.GONE);
        ((BaseActivity) getActivity()).gotoAccount();
        getActivity().finish();
    }

    private void showLogoutDialog() {
        final BaseAlertDialog dialog = new BaseAlertDialog(getActivity());
        dialog.setTitle(R.string.logout_title);
        dialog.setMessage(getResources().getString(R.string.logout_summary,
                LoginManager.getInstance().getSystemAccountId()));
        dialog.setPositiveButton(R.string.dialog_ask_cancel, new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.dialog_ask_ok, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoader != null) {
                    getLoaderManager().destroyLoader(USERINFO_LOADER);
                    mLoader = null;
                }
                showView(mAccountViewNotLoginContainer);
                hideView(mAccountLoginContainer);
                LoginManager.getInstance().logout();
            }
        });
        dialog.show();
    }

    private void showView(View view) {
        if (view == null) {
            return;
        }

        if (view.getVisibility() == View.GONE) {
            view.startAnimation(AnimationUtils.loadAnimation(view.getContext(),
                    R.anim.appear));
            view.setVisibility(View.VISIBLE);
        }
    }

    private void hideView(View view) {
        if (view == null) {
            return;
        }
        if (view.getVisibility() == View.VISIBLE) {
            if (view.isShown()) {
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(),
                        R.anim.disappear));
            }
            view.setVisibility(View.GONE);
        }
    }

}
