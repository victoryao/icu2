package com.xiaomi.xms.sales.xmsf.account.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;

public class LoginFailureFragment extends Fragment implements View.OnClickListener {

    private Button mButtonCancel;

    private Button mButtonRetry;

    private OnLoginFailureInterface mOnLoginFailureInterface;
    private GetUserInputInterface mInputInterface;

    private String mUserName;

    private String mPwd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.micloud_login_failure, container, false);

        mButtonCancel = (Button) v.findViewById(R.id.btn_cancel);
        mButtonRetry = (Button) v.findViewById(R.id.btn_retry);

        mButtonCancel.setOnClickListener(this);
        mButtonRetry.setOnClickListener(this);

        Bundle args = getArguments();
        if (args != null) {
            ToastUtil.show(getActivity(), args.getString("reason"));
            mUserName = args.getString("username");
            mPwd = args.getString("pwd");
        }
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (v == mButtonCancel) {
            if (mOnLoginFailureInterface != null) {
                mOnLoginFailureInterface.onCancelLoginAfterFailure();
            }
        } else if (v == mButtonRetry) {
            if (mOnLoginFailureInterface != null) {
                Utils.SoftInput.hide(getActivity(), mButtonRetry.getWindowToken());
                if (mInputInterface != null) {
                    mUserName = mInputInterface.getUserName();
                    mPwd = mInputInterface.getUserPwd();
                }
                mOnLoginFailureInterface.onRetryLoginAfterFailure(mUserName, mPwd);
            }
        }
    }

    public void setOnLoginFailureInterface(OnLoginFailureInterface onLoginFailureInterface) {
        mOnLoginFailureInterface = onLoginFailureInterface;
    }

    public static interface OnLoginFailureInterface {
        public void onCancelLoginAfterFailure();
        public void onRetryLoginAfterFailure(String username, String pwd);
    }

    public void setGetUserInputInterface(GetUserInputInterface inputInterface) {
        mInputInterface = inputInterface;
    }

    public static interface GetUserInputInterface {
        public String getUserName();
        public String getUserPwd();
    }
}
