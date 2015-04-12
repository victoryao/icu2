
package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class SettingFragment extends BaseFragment {
    private static final String TAG = "SettingFragment";
    private TextView mPrefTitleVersion;
    private TextView mPrefTitleUserName;
    private TextView mPrefTitleUserId;
    private View mPrefItemCheckVersion;
    private Button mLogoutBtn;
    private CheckUpdateListener mCheckUpdateListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_fragment, container, false);
        mPrefItemCheckVersion = view.findViewById(R.id.pref_item_check_version);
        mPrefTitleVersion = (TextView) view.findViewById(R.id.version_name);
        mPrefTitleUserName = (TextView) view.findViewById(R.id.user_name);
        mPrefTitleUserId = (TextView) view.findViewById(R.id.user_miliao);
        mLogoutBtn = (Button) view.findViewById(R.id.btn_logout);
        mPrefItemCheckVersion.setOnClickListener(mOnClickListener);
        mLogoutBtn.setOnClickListener(mOnClickListener);
        mPrefTitleUserName.setText(Utils.Preference
                .getStringPref(getActivity(), Constants.Account.PREF_USER_NAME, null));
        mPrefTitleUserId.setText(LoginManager.getInstance().getUserId());
        mPrefTitleVersion.setText(getString(R.string.pref_title_version, getVersionName()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pref_item_check_version:
                    if (mCheckUpdateListener != null) {
                        mCheckUpdateListener.onCheckUpdate();
                    }
                    break;
                case R.id.btn_logout:
                    LoginManager.getInstance().logout();
                    getActivity().finish();
                    break;
                default:
                    break;
            }
        }
    };

    private String getVersionName() {
        return Device.SHOP_VERSION_STRING;
    }

    public void setCheckUpdateListener(CheckUpdateListener l) {
        mCheckUpdateListener = l;
    }

    public interface CheckUpdateListener {
        public void onCheckUpdate();
    }

}
